package com.evo.poker.parser

import org.scalatest._
import org.scalatest.matchers.should.Matchers

import com.evo.poker.logic.Rank.{Four, Ten}
import com.evo.poker.logic.Suit.{Clubs, Spades}
import com.evo.poker.logic.{Card, Rules}

class PokerParserTest extends FlatSpec with Matchers with EitherValues {
  private val texas = PokerParser.of(Rules.texas())

  "Parser" should "parse cards" in {
    texas.parseCard("4c") shouldBe Right(Card(Four, Clubs))
    texas.parseCard("Ts") shouldBe Right(Card(Ten, Spades))
  }

  it should "parse card lists" in {
    texas.parseCards("4cTs4cTs", 4) shouldBe Right(
      List(
        Card(Four, Clubs),
        Card(Ten, Spades),
        Card(Four, Clubs),
        Card(Ten, Spades)
      )
    )
  }
}
