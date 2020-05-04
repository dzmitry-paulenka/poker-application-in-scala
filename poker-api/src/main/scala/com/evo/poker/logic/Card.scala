package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._

sealed abstract class Rank(val symbol: Char, val value: Int)

object Rank {

  case object Two   extends Rank('2', 2)
  case object Three extends Rank('3', 3)
  case object Four  extends Rank('4', 4)
  case object Five  extends Rank('5', 5)
  case object Six   extends Rank('6', 6)
  case object Seven extends Rank('7', 7)
  case object Eight extends Rank('8', 8)
  case object Nine  extends Rank('9', 9)
  case object Ten   extends Rank('T', 10)
  case object Jack  extends Rank('J', 11)
  case object Queen extends Rank('Q', 12)
  case object King  extends Rank('K', 13)
  case object Ace   extends Rank('A', 14)

  val ranks = List(
    Rank.Two,
    Rank.Three,
    Rank.Four,
    Rank.Five,
    Rank.Six,
    Rank.Seven,
    Rank.Eight,
    Rank.Nine,
    Rank.Ten,
    Rank.Jack,
    Rank.Queen,
    Rank.King,
    Rank.Ace
  )

  private val ranksMap =
    ranks.map(r => r.symbol -> r).toMap

  def fromChar(symbol: Char): Option[Rank] =
    ranksMap.get(symbol)

  implicit val rankOrder: Order[Rank] =
    Order.by[Rank, Int](_.value)
}

sealed abstract class Suit(val symbol: Char)

object Suit {

  case object Clubs    extends Suit('c') // ♣
  case object Diamonds extends Suit('d') // ♦
  case object Hearts   extends Suit('h') // ♥
  case object Spades   extends Suit('s') // ♠

  val suits = List(
    Suit.Clubs,
    Suit.Diamonds,
    Suit.Hearts,
    Suit.Spades
  )

  private val suitsMap =
    suits.map(r => r.symbol -> r).toMap

  def fromChar(symbol: Char): Option[Suit] =
    suitsMap.get(symbol)

  implicit val suitOrder: Order[Suit] =
    Order.by[Suit, Char](_.symbol)
}

final case class Card(rank: Rank, suit: Suit) {
  override def toString: String =
    s"${rank.symbol}${suit.symbol}"
}

object Card {
  type CardList = List[Card]
  type Board    = CardList
  type Hand     = CardList

  val all: CardList = for {
    r <- Rank.ranks
    s <- Suit.suits
  } yield Card(r, s)

  implicit val cardOrder: Order[Card] =
    Order.by[Card, (Rank, Suit)](c => (c.rank, c.suit))
}
