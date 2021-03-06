package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._

import com.evo.poker.logic.Card.Hand

final case class Player(
  id: String,
  balance: Int = 0,
  hand: Hand = Nil,
  gameBet: Int = 0,
  roundBet: Int = 0,
  resultMoneyWon: Int = 0,
  resultCombo: Option[Combination] = None,
  actedInRound: Boolean = false,
  allIn: Boolean = false,
  sittingOut: Boolean = true
) {

  def sitOut(): Player = {
    copy(
      hand = Nil,
      gameBet = 0,
      roundBet = 0,
      resultMoneyWon = 0,
      resultCombo = None,
      actedInRound = false,
      allIn = false,
      sittingOut = true
    )
  }

  def enterRound(hand: Hand): Player =
    copy(
      hand = hand,
      gameBet = 0,
      roundBet = 0,
      resultMoneyWon = 0,
      resultCombo = None,
      actedInRound = false,
      allIn = false,
      sittingOut = false
    )

  def endRound(): Player = {
    copy(
      balance = balance + resultMoneyWon,
      hand = Nil,
      gameBet = 0,
      roundBet = 0,
      resultMoneyWon = 0,
      resultCombo = None,
      actedInRound = false,
      allIn = false,
      sittingOut = true
    )
  }

  def enterNewPhase(): OrError[Player] = {
    copy(
      roundBet = 0,
      actedInRound = false
    ).asRight
  }

  def acted(): Player = {
    copy(actedInRound = true)
  }

  def notActed(): Player = {
    copy(actedInRound = false)
  }

  def check(gameRoundBet: Int): OrError[Player] = {
    Either.cond(
      roundBet == gameRoundBet,
      this.copy(actedInRound = true),
      s"Player $id can't check: should have bet $gameRoundBet this round, but only bet ${roundBet}"
    )
  }

  def call(gameRoundBet: Int): OrError[Player] =
    bet(gameRoundBet - roundBet)

  def raise(gameRoundBet: Int, raiseAmount: Int): OrError[Player] = {
    if (raiseAmount <= 0)
      s"Player $id can't raise amount for less-or-eq than 0".asLeft
    else if (balance < gameRoundBet + raiseAmount - roundBet)
      s"Player $id don't have funds to raise $raiseAmount, current balance is $balance".asLeft
    else
      bet(gameRoundBet + raiseAmount - roundBet)
  }

  def bet(amount: Int, blind: Boolean = false): OrError[Player] = {
    require(amount >= 0)

    val nAmount = if (balance <= amount) balance else amount
    copy(
      balance = balance - nAmount,
      gameBet = gameBet + nAmount,
      roundBet = roundBet + nAmount,
      allIn = balance == nAmount,
      actedInRound = !blind
    ).asRight
  }
}

object Player {}
