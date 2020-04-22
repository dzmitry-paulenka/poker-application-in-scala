package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._
import com.softwaremill.quicklens._

import com.evo.poker.logic.Card.CardList
import com.evo.poker.util.Util.ift

case class PlayerCombination(player: Player, combination: Combination)

case class Game(
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

  lazy val currentPlayer: Player =
    activePlayers(currentPlayerIndex)

  lazy val dealerPlayer: Player =
    activePlayers(dealerPlayerIndex)

  lazy val activePlayers = {
    players.filterNot(_.sittingOut)
  }

  def player(playerId: String): Player =
    players.find(_.id == playerId).get

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
        player.id == currentPlayer.id,
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

  def adjustCurrentIndexOnLeave(index: Int, leavingPlayerId: String): Int = {
    val leavingIndex: Int = activePlayers.indexWhere(_.id == leavingPlayerId)
    ift(leavingIndex >= 0 && index > leavingIndex, index - 1, index)
  }

  def transition(transition: GameTransition): OrError[Game] = {
    transition match {
      case Deal                    => deal()
      case Join(playerId, balance) => join(playerId, balance)
      case Leave(playerId)         => leave(playerId)
      case Check(playerId)         => check(playerId)
      case Call(playerId)          => call(playerId)
      case Raise(playerId, amount) => raise(playerId, amount)
      case Fold(playerId)          => fold(playerId)
      case Finish                  => finish()
    }
  }

  def deal(): OrError[Game] = {
    if (!Phase.dealAllowed(phase))
      return s"Can't deal in phase $phase".asLeft

    var nDeck             = deck
    var nActivePlayersCnt = 0

    val nPlayers = players.map { player =>
      if (player.balance <= 0 || nActivePlayersCnt >= 9) {
        player.sitOut()
      } else {
        val (hand, deck) = nDeck.deal(rules.handSize)
        nDeck = deck

        nActivePlayersCnt += 1
        player.enterGame(hand = hand).acted()
      }
    }

    if (nActivePlayersCnt < 3)
      return "Game can only be started with at least 3 players".asLeft

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

  def join(playerId: String, balance: Int): OrError[Game] = {
    if (balance < rules.minimumBalance)
      return s"Joining player balance ($balance) should be at least ${rules.minimumBalance}".asLeft

    if (players.exists(_.id == playerId))
      return this.asRight

    copy(
      players = players :+ Player(playerId, balance).sitOut()
    ).asRight
  }

  def leave(playerId: String): OrError[Game] = {
    if (currentPlayerIndex >= 0 && playerId == currentPlayer.id)
      return fold(playerId).flatMap(_.leave(playerId))

    for {
      _ <- requirePlayer(playerId)

      nPlayers            = players.filterNot(_.id == playerId)
      nCurrentPlayerIndex = adjustCurrentIndexOnLeave(currentPlayerIndex, playerId)

      ng <- copy(
        players = nPlayers,
        currentPlayerIndex = nCurrentPlayerIndex
      ).asRight

    } yield ng
  }

  def check(playerId: String): OrError[Game] = {
    if (!Phase.betsAllowed(phase))
      return s"Can't check in phase $phase".asLeft

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
      return s"Can't call in phase $phase".asLeft

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
      return s"Can't raise in phase $phase".asLeft

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
      return s"Can't fold in phase $phase".asLeft

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

  def finish(): OrError[Game] = {
    if (phase != Showdown)
      return s"Can't finish in phase $phase".asLeft

    val nPlayers = players.map(
      _.endGame()
    )

    Game(
      rules = rules,
      deck = deck.reset(),
      players = nPlayers
    ).asRight
  }

  private def makeProgress(): OrError[Game] = {
    val allActed     = activePlayers.forall(_.actedInRound)
    val nPhase       = ift(allActed, Phase.next(phase), phase)
    val phaseChanged = phase != nPhase

    if (nPhase == Showdown || activePlayers.length < 2) {
      return doShowdown()
    }

    val nCurrentPlayerIndex =
      (ift(allActed, dealerPlayerIndex, currentPlayerIndex) + 1) % activePlayers.length

    val (cs, d)   = deck.deal(Phase.cardsToDeal(nPhase))
    val nDeck     = ift(phaseChanged, d, deck)
    val nBoard    = ift(phaseChanged, board ++ cs, board)
    val nRoundBet = ift(phaseChanged, 0, roundBet)

    for {
      nPlayers <- players.traverse { player =>
        ift(allActed, player.enterNewRound(), player.asRight)
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

  private def doShowdown(): OrError[Game] = {
    require(activePlayers.nonEmpty)

    if (activePlayers.length == 1 && board.length < 5) {
      val (cards, nDeck) = deck.deal(5 - board.length)
      return copy(
        deck = nDeck,
        board = board ++ cards
      ).doShowdown()
    }

    require(board.length == 5)

    val winners = activePlayers
      .map(bestCombinationForPlayerInGame)
      .groupMap(_.combination)(_.player)
      .maxBy(_._1)
      ._2

    require(winners.nonEmpty)

    val potDiv = pot / winners.length
    val potRem = pot % winners.length
    val nWinners = winners.mapWithIndex { (player, index) =>
      player.copy(resultMoneyWon = potDiv + ift(index < potRem, 1, 0))
    }

    val nPlayers = nWinners.foldLeft(players) { (nPlayers, winner) =>
      val pIndex = players.indexWhere(_.id == winner.id)
      nPlayers.modify(_.at(pIndex)).setTo(winner)
    }

    copy(
      phase = Showdown,
      players = nPlayers
    ).asRight
  }

  private def bestCombinationForPlayerInGame(player: Player): PlayerCombination = {
    val minHandUse = rules.minHandUse
    val maxHandUse = rules.maxHandUse
    val hand       = player.hand

    val combos = for {
      handUse    <- minHandUse to maxHandUse
      handCards  <- hand.combinations(handUse)
      boardCards <- board.combinations(5 - handUse)
    } yield Combination.of(handCards ++ boardCards)

    PlayerCombination(player, combos.max)
  }

}

object Game {
  def create(rules: Rules, deck: Deck): Game = {
    Game(rules, deck)
  }
}
