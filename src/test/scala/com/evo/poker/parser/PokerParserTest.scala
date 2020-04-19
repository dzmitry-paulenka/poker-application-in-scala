package com.evo.poker.parser

import com.evo.poker.model.Rank.{Four, Ten}
import com.evo.poker.model.Suit.{Clubs, Spades}
import com.evo.poker.model.{Card, Rules}
import org.scalatest._
import org.scalatest.matchers.should.Matchers

class PokerParserTest extends FlatSpec with Matchers with EitherValues {
  private val texas = PokerParser.of(Rules.Texas)

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
      ))
  }
}
