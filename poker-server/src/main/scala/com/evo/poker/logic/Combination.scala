package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._

import scala.collection.MapView

import com.evo.poker.logic.Rank._
import com.evo.poker.logic.Tier._

sealed abstract class Tier(val value: Int, val name: String)

object Tier {
  case object StraightFlush       extends Tier(10, "Straight Flush")
  case object LowAceStraightFlush extends Tier(9, "Straight Flush")
  case object FourOfKind          extends Tier(8, "Four Of Kind")
  case object FullHouse           extends Tier(7, "Full House")
  case object Flush               extends Tier(6, "Flush")
  case object Straight            extends Tier(5, "Straight")
  case object LowAceStraight      extends Tier(4, "Straight")
  case object ThreeOfKind         extends Tier(3, "Three Of Kind")
  case object TwoPairs            extends Tier(2, "Two Pairs")
  case object Pair                extends Tier(1, "Pair")
  case object HighCard            extends Tier(0, "High Card")

  implicit val tierOrder: Order[Tier] =
    Order.by[Tier, Int](_.value)

}

sealed abstract case class Combination private (
  tier: Tier,
  private val rankListsSortedByCount: List[List[Rank]]
)

object Combination {

  implicit val combinationOrder: Order[Combination] =
    (x: Combination, y: Combination) => {
      val cmps: Seq[Int] = x.tier.compare(y.tier) +: (for {
        (rs1, rs2) <- x.rankListsSortedByCount.zip(y.rankListsSortedByCount)
        (r1, r2)   <- rs1.zip(rs2)
      } yield r1.compare(r2))

      cmps.find(_ != 0).getOrElse(0)
    }

  def of(_cards: List[Card]): Combination = {
    val cards = _cards.sorted.reverse
    val rankToCount = cards
      .groupBy(_.rank)
      .view
      .mapValues(_.size)

    val countToRanks = rankToCount
      .groupMap(_._2)(_._1)
      .view
      .mapValues { _.toList.sorted.reverse }
      .toMap

    val rankListsSortedByCount: List[List[Rank]] = countToRanks
      .toList
      .sortBy(_._1)
      .reverse
      .map(_._2)

    val tier = computeTier(cards, rankToCount, countToRanks)

    new Combination(tier, rankListsSortedByCount) {}
  }

  private def computeTier(cards: List[Card], rankToCount: MapView[Rank, Int], countToRanks: Map[Int, List[Rank]]) = {
    val isLowAceStraight = rankToCount.size == 5 &&
      (cards(0).rank == Ace && cards(1).rank == Five && cards(4).rank == Two)

    val isNormalStraight = rankToCount.size == 5 &&
      (cards(0).rank.value - cards(4).rank.value == 4)

    val isFlush = cards.map(_.suit).distinct.size == 1

    if (isNormalStraight && isFlush)
      StraightFlush
    else if (isLowAceStraight && isFlush)
      LowAceStraightFlush
    else if (countToRanks.contains(4))
      FourOfKind
    else if (countToRanks.contains(3) && countToRanks.contains(2))
      FullHouse
    else if (isFlush)
      Flush
    else if (isNormalStraight)
      Straight
    else if (isLowAceStraight)
      LowAceStraight
    else if (countToRanks.contains(3))
      ThreeOfKind
    else if (countToRanks.get(2).exists(_.size == 2))
      TwoPairs
    else if (countToRanks.get(2).exists(_.size == 1))
      Pair
    else
      HighCard
  }
}
