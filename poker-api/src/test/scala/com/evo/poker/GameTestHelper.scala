package com.evo.poker

import cats._
import cats.instances.all._
import cats.syntax.all._
import org.scalatest.EitherValues
import org.scalatest.matchers.should.Matchers

import com.evo.poker.logic.Card.CardList
import com.evo.poker.logic._
import com.evo.poker.parser.PokerParser

trait GameTestHelper extends Matchers with EitherValues {
  val rules  = Rules.texas(smallBlind = 1, buyIn = 100)
  val parser = PokerParser.of(rules)

  def card(s: String): Card =
    parser.parseCard(s).right.value

  def cards(s: String): CardList =
    parser.parseCards(s).right.value

  def gameFromDeck(deckString: String): OrError[Game] = {
    parser
      .parseCards(deckString)
      .map { cards => Game.create(rules, Deck.of(cards)) }
  }

  def randomGame(): OrError[Game] =
    Game.create(rules, Deck.random()).asRight

  def sampleGame(playerCnt: Int, deckString: String = ""): OrError[Game] =
    constructGame(
      if (deckString.isEmpty) randomGame() else gameFromDeck(deckString),
      (0 until playerCnt).map(i => g => g.join(('a' + i).toChar.toString))
    )

  def constructGame(initial: OrError[Game], play: Seq[Game => OrError[Game]]): OrError[Game] = {
    play.foldLeft(initial) { (g, op) => g.flatMap(op(_)) }
  }

  def validate(g: Game)(validation: Game => Unit): OrError[Game] = {
    validation(g)
    g.asRight
  }

  def assertValidGame(result: OrError[Game]): Any = {
    result shouldBe Symbol("right")
  }

  def assertErrorGame(error: String)(result: OrError[Game]): Any = {
    result.left.value should include(error)
  }

}
