package com.evo.poker.services.models

import io.circe.generic.JsonCodec

import com.evo.poker.logic.Card.Hand
import com.evo.poker.logic.{Card, Game, Phase, Showdown}
import com.evo.poker.services.http.Codecs._
import com.evo.poker.util.Util.ift

@JsonCodec
case class PlayerProjection(
  id: String,
  balance: Int = 0,
  hand: Hand = Nil,
  gameBet: Int = 0,
  roundBet: Int = 0,
  resultMoneyWon: Int = 0,
  actedInRound: Boolean = false,
  allIn: Boolean = false,
  sittingOut: Boolean = true
)

@JsonCodec
case class GameProjection(
  id: String,
  phase: Phase,
  board: List[Card],
  pot: Int,
  roundBet: Int,
  players: Vector[PlayerProjection],
  currentPlayerIndex: Int,
  dealerPlayerIndex: Int
)

object GameProjection {
  def of(playerId: String, gameId: String, game: Game): GameProjection = {
    val players = game.players.map { player =>
      val handToSend = ift(playerId == player.id || game.phase == Showdown, player.hand, Nil)
      PlayerProjection(
        id = player.id,
        balance = player.balance,
        hand = handToSend,
        gameBet = player.gameBet,
        roundBet = player.roundBet,
        resultMoneyWon = player.resultMoneyWon,
        actedInRound = player.actedInRound,
        allIn = player.allIn,
        sittingOut = player.sittingOut
      )
    }

    GameProjection(
      id = gameId,
      game.phase,
      game.board,
      game.pot,
      game.roundBet,
      players,
      game.currentPlayerIndex,
      game.dealerPlayerIndex
    )
  }
}
