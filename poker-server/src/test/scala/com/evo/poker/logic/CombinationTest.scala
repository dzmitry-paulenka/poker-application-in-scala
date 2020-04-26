package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._
import org.scalatest._
import org.scalatest.matchers.should.Matchers

import com.evo.poker.parser.PokerParser

class CombinationTest extends FlatSpec with Matchers {
  private val texas = PokerParser.of(Rules.texas())

  "Combination" should "correctly determine tier" in {
    parseCombo("Qs2dQc2sQh").map(_.tier) shouldBe Right(Tier.FullHouse)
    parseCombo("7s2sAsTs8s").map(_.tier) shouldBe Right(Tier.Flush)
    parseCombo("7cJs9hTd8s").map(_.tier) shouldBe Right(Tier.Straight)
    parseCombo("2c3s4hAd5s").map(_.tier) shouldBe Right(Tier.LowAceStraight)
    parseCombo("2c3c4cAc5c").map(_.tier) shouldBe Right(Tier.LowAceStraightFlush)
  }

  it should "compare correctly" in {
    // re-ordering
    cmp("2c3s4hAd5s", "Ad2c4h3s5s") shouldBe Right(0)

    // Flush vs Flush
    cmp("6s7sTs4sQs", "5s7sTs4sQs") shouldBe Right(1)

    // Flush vs Straight
    cmp("5s7sTs4sQs", "AcKcQcJhTh") shouldBe Right(1)

    // Flush vs low-ace StraightFlush
    cmp("5s7sTs4sQs", "2c3c4cAc5c") shouldBe Right(-1)

    // low-ace Straight vs Three
    cmp("2c3s4hAd5s", "KcQcKsJdKd") shouldBe Right(1)

    // low-ace Straight vs Straight
    cmp("2c3s4hAd5s", "2c3s4h5s6s") shouldBe Right(-1)
    cmp("2c3s4hAd5s", "JcQsKhAdTs") shouldBe Right(-1)

    // high card with last card decider
    cmp("2c6s5h8d7s", "3c6s5h8d7s") shouldBe Right(-1)

    // pair with last card decider
    cmp("2c2sAd5s4h", "2c2sAd5s3h") shouldBe Right(1)
  }

  def cmp(combo1: String, combo2: String): Either[String, Int] =
    for {
      c1 <- parseCombo(combo1)
      c2 <- parseCombo(combo2)
    } yield c1.compare(c2)

  def parseCombo(comboString: String): Either[String, Combination] =
    texas
      .parseCards(comboString, 5)
      .map(Combination.of)
}
