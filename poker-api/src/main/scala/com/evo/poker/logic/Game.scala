package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._
import com.softwaremill.quicklens._

import com.evo.poker.logic.Card.CardList
import com.evo.poker.util.Util.ift

final case class Game(
  name: String,
  rules: Rules,
  deck: Deck,
  phase: Phase = PreDeal,
  board: CardList = Nil,
  pot: Int = 0,
  roundBet: Int = 0,
  players: Vector[Player] = Vector.empty[Player],
  currentPlayerIndex: Int = -1,
  dealerPlayerIndex: Int = -1
) {

  lazy val currentPlayer: Option[Player] =
    if (currentPlayerIndex >= 0 && activePlayers.nonEmpty)
      Some(activePlayers(currentPlayerIndex))
    else
      None

  lazy val dealerPlayer: Player =
    activePlayers(dealerPlayerIndex)

  lazy val activePlayers: Vector[Player] = {
    players.filterNot(_.sittingOut)
  }

  def isCurrentPlayer(playerId: String): Boolean =
    currentPlayer.exists(_.id == playerId)

  def player(playerId: String): Option[Player] =
    players.find(_.id == playerId)

  def hasPlayer(playerId: String): Boolean =
    player(playerId).isDefined

  def activePlayer(playerId: String): Player =
    activePlayers.find(_.id == playerId).get

  def requirePlayer(playerId: String): OrError[Player] =
    players.find(_.id == playerId).toRight(s"Player with id: $playerId not found")

  def requireActivePlayer(playerId: String): OrError[Player] =
    activePlayers.find(_.id == playerId).toRight(s"Active Player with id: $playerId not found")

  def requireCurrentPlayer(playerId: String): OrError[Player] =
    for {
      player <- requirePlayer(playerId)
      _ <- Either.cond(
        isCurrentPlayer(playerId),
        player,
        s"Player with id: $playerId, is trying to make a move out of turn"
      )
    } yield player

  def requirePlayerIndex(playerId: String): OrError[Int] = {
    val pIndex = players.indexWhere(_.id == playerId)
    if (pIndex < 0)
      s"Player with id: $playerId not found".asLeft
    else
      pIndex.asRight
  }

  def requireActivePlayerIndex(playerId: String): OrError[Int] = {
    val paIndex = activePlayers.indexWhere(_.id == playerId)
    if (paIndex < 0)
      s"Active player with id: $playerId not found".asLeft
    else
      paIndex.asRight
  }

  def adjustIndexOnLeave(index: Int, leavingPlayerId: String): Int = {
    val leavingIndex: Int = activePlayers.indexWhere(_.id == leavingPlayerId)
    ift(leavingIndex >= 0 && index > leavingIndex, index - 1, index)
  }

  def canDeal: Boolean =
    this.deal().isRight

  def canNextRound: Boolean =
    this.nextRound().isRight

  def transition(transition: GameTransition): OrError[Game] = {
    transition match {
      case Deal                    => deal()
      case Join(playerId)          => join(playerId)
      case Leave(playerId)         => leave(playerId)
      case Check(playerId)         => check(playerId)
      case Call(playerId)          => call(playerId)
      case Raise(playerId, amount) => raise(playerId, amount)
      case Fold(playerId)          => fold(playerId)
      case NextRound               => nextRound()
      case End                     => finish()
    }
  }

  def deal(): OrError[Game] = {
    val nActivePlayersCnt = players.count(_.balance > 0)

    if (phase != PreDeal)
      s"Can't deal in phase $phase".asLeft
    else if (nActivePlayersCnt < 2)
      "Game can only be started with at least 2 players with positive balance".asLeft
    else {
      var nDeck = deck
      val nPlayers = players.map { player =>
        if (player.balance <= 0) {
          player.sitOut()
        } else {
          val (hand, deck) = nDeck.deal(rules.handSize)
          nDeck = deck

          player.enterRound(hand = hand).acted()
        }
      }

      val nActivePlayers = nPlayers.filterNot(_.sittingOut)

      val dealerIndex   = (dealerPlayerIndex + 1) % nActivePlayersCnt
      val sbPlayerIndex = (dealerIndex + 1) % nActivePlayersCnt
      val bbPlayerIndex = (dealerIndex + 2) % nActivePlayersCnt
      val sbPlayer      = nActivePlayers(sbPlayerIndex)
      val bbPlayer      = nActivePlayers(bbPlayerIndex)

      for {
        ng <- copy(
          deck = nDeck,
          board = Nil,
          pot = 0,
          roundBet = 0,
          players = nPlayers,
          currentPlayerIndex = dealerIndex,
          dealerPlayerIndex = dealerIndex
        ).makeProgress()

        ng <- ng.blindBet(sbPlayer.id, rules.smallBlind)
        ng <- ng.blindBet(bbPlayer.id, rules.bigBlind)

      } yield ng
    }
  }

  private def blindBet(playerId: String, amount: Int): OrError[Game] = {
    for {
      player <- requireCurrentPlayer(playerId)
      pIndex <- requirePlayerIndex(playerId)

      nPlayer <- player.bet(amount, blind = true)
      nPlayers = players.modify(_.at(pIndex)).setTo(nPlayer)
      nPot     = pot + (player.balance - nPlayer.balance)

      ng <- copy(
        pot = nPot,
        roundBet = amount,
        players = nPlayers
      ).makeProgress()

    } yield ng
  }

  def join(playerId: String): OrError[Game] = {
    if (phase == Ended)
      s"Can't join after the game is ended".asLeft
    else if (hasPlayer(playerId))
      this.asRight
    else if (players.size == rules.playersLimit)
      s"Game already has the maximum number of players".asLeft
    else
      copy(
        players = players :+ Player(playerId, rules.buyIn).sitOut()
      ).asRight
  }

  def leave(playerId: String): OrError[Game] = {
    if (currentPlayerIndex >= 0 && isCurrentPlayer(playerId))
      fold(playerId).flatMap(_.leave(playerId))
    else
      for {
        _ <- requirePlayer(playerId)

        nPlayers            = players.filterNot(_.id == playerId)
        nCurrentPlayerIndex = adjustIndexOnLeave(currentPlayerIndex, playerId)
        nDealerIndex        = adjustIndexOnLeave(dealerPlayerIndex, playerId)

        ng = copy(
          players = nPlayers,
          currentPlayerIndex = nCurrentPlayerIndex,
          dealerPlayerIndex = nDealerIndex
        )

        ng <- ift(ng.activePlayers.length == 1, ng.doShowdown(), ng.asRight)
      } yield ng
  }

  def check(playerId: String): OrError[Game] = {
    if (!Phase.betsAllowed(phase))
      s"Can't check in phase $phase".asLeft
    else
      for {
        player <- requireCurrentPlayer(playerId)
        pIndex <- requirePlayerIndex(playerId)

        nPlayer <- player.check(roundBet)
        nPlayers = players.modify(_.at(pIndex)).setTo(nPlayer)

        ng <- copy(
          players = nPlayers
        ).makeProgress()

      } yield ng
  }

  def call(playerId: String): OrError[Game] = {
    if (!Phase.betsAllowed(phase))
      s"Can't call in phase $phase".asLeft
    else
      for {
        player <- requireCurrentPlayer(playerId)
        pIndex <- requirePlayerIndex(playerId)

        nPlayer <- player.call(roundBet)
        nPlayers = players.modify(_.at(pIndex)).setTo(nPlayer)
        nPot     = pot + (player.balance - nPlayer.balance)

        ng <- copy(
          pot = nPot,
          players = nPlayers
        ).makeProgress()

      } yield ng
  }

  def raise(playerId: String, raiseAmount: Int): OrError[Game] = {
    if (!Phase.betsAllowed(phase))
      s"Can't raise in phase $phase".asLeft
    else
      for {
        player <- requireCurrentPlayer(playerId)
        pIndex <- requirePlayerIndex(playerId)

        nPlayer <- player.raise(roundBet, raiseAmount)
        nPlayers =
          players
            .map(_.notActed())
            .modify(_.at(pIndex))
            .setTo(nPlayer)

        nPot = pot + (player.balance - nPlayer.balance)

        ng <- copy(
          pot = nPot,
          roundBet = roundBet + raiseAmount,
          players = nPlayers
        ).makeProgress()

      } yield ng
  }

  def fold(playerId: String): OrError[Game] = {
    if (!Phase.betsAllowed(phase))
      s"Can't fold in phase $phase".asLeft
    else
      for {
        player <- requireCurrentPlayer(playerId)
        pIndex <- requirePlayerIndex(playerId)

        nPlayer             = player.sitOut()
        nPlayers            = players.modify(_.at(pIndex)).setTo(nPlayer)
        nCurrentPlayerIndex = currentPlayerIndex - 1

        ng <- copy(
          players = nPlayers,
          currentPlayerIndex = nCurrentPlayerIndex
        ).makeProgress()

      } yield ng
  }

  def nextRound(): OrError[Game] = {
    if (phase != Showdown)
      s"Can't finish in phase $phase".asLeft
    else
      copy(
        deck = deck.reset(),
        phase = PreDeal,
        board = Nil,
        pot = 0,
        roundBet = 0,
        players = players.map(_.endRound())
      ).asRight
  }

  def finish(): OrError[Game] = {
    copy(
      phase = Ended
    ).asRight
  }

  private def makeProgress(): OrError[Game] = {
    val allActed     = activePlayers.forall(_.actedInRound)
    val nPhase       = ift(allActed, Phase.nextPlayable(phase), phase)
    val phaseChanged = phase != nPhase

    if (nPhase == Showdown || activePlayers.length < 2) {
      doShowdown()
    } else {
      val nCurrentPlayerIndex =
        (ift(allActed, dealerPlayerIndex, currentPlayerIndex) + 1) % activePlayers.length

      val (cs, d)   = deck.deal(Phase.cardsToDeal(nPhase))
      val nDeck     = ift(phaseChanged, d, deck)
      val nBoard    = ift(phaseChanged, board ++ cs, board)
      val nRoundBet = ift(phaseChanged, 0, roundBet)

      for {
        nPlayers <- players.traverse { player =>
          ift(allActed, player.enterNewPhase(), player.asRight)
        }

        ng <- copy(
          deck = nDeck,
          phase = nPhase,
          board = nBoard,
          roundBet = nRoundBet,
          players = nPlayers,
          currentPlayerIndex = nCurrentPlayerIndex
        ).asRight

      } yield ng
    }
  }

  private def doShowdown(): OrError[Game] = {
    require(activePlayers.nonEmpty)

    var nWinners = Vector.empty[Player]
    var nPlayers = players
    if (activePlayers.length == 1) {
      nWinners = activePlayers
    } else {
      require(board.length == 5)

      nPlayers = nPlayers
        .map(fillBestComboForPlayerInGame)

      nWinners = nPlayers
        .filter(_.resultCombo.isDefined)
        .groupBy(_.resultCombo.get)
        .maxBy(_._1)
        ._2
    }

    require(nWinners.nonEmpty)

    val potDiv = pot / nWinners.length
    val potRem = pot % nWinners.length
    nWinners = nWinners.mapWithIndex { (player, index) =>
      player.copy(resultMoneyWon = potDiv + ift(index < potRem, 1, 0))
    }

    nPlayers = nWinners.foldLeft(nPlayers) { (nPlayers, winner) =>
      val pIndex = nPlayers.indexWhere(_.id == winner.id)
      nPlayers.modify(_.at(pIndex)).setTo(winner)
    }

    copy(
      phase = Showdown,
      currentPlayerIndex = -1,
      players = nPlayers
    ).asRight
  }

  private def fillBestComboForPlayerInGame(player: Player): Player = {
    if (player.sittingOut) {
      player.copy(resultCombo = None)
    } else {
      val minHandUse = rules.minHandUse
      val maxHandUse = rules.maxHandUse
      val hand       = player.hand

      val combos = for {
        handUse    <- minHandUse to maxHandUse
        handCards  <- hand.combinations(handUse)
        boardCards <- board.combinations(5 - handUse)
      } yield Combination.of(handCards ++ boardCards)

      player.copy(resultCombo = Some(combos.max))
    }
  }
}

object Game {
  def create(rules: Rules, deck: Deck, name: String = "noname"): Game = {
    Game(name, rules, deck)
  }
}
