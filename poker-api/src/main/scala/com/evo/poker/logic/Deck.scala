package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._

import scala.util.Random

import com.evo.poker.logic.Card.CardList

sealed abstract class Deck() {
  def reset(): Deck

  def deal(n: Int): (CardList, Deck)

  def deal1(): (Card, Deck) =
    deal(1).leftMap(_.head)
}

final class RandomDeck(cards: List[Card], resetSeed: Long) extends Deck {

  override def reset(): Deck =
    Deck.random(resetSeed);

  override def deal(n: Int): (CardList, Deck) = {
    (cards.take(n), new RandomDeck(cards.drop(n), resetSeed))
  }
}

final class CardListDeck(cards: List[Card]) extends Deck {

  override def reset(): Deck = this

  override def deal(n: Int): (CardList, Deck) = {
    (cards.take(n), new CardListDeck(cards.drop(n)))
  }
}

object Deck {

  def of(cards: List[Card]): Deck = {
    new CardListDeck(cards)
  }

  def random(): Deck =
    random(System.nanoTime())

  def random(seed: Long): Deck = {
    val random    = new Random(seed)
    val cards     = random.shuffle(Card.all)
    val resetSeed = random.nextLong()

    new RandomDeck(
      cards,
      resetSeed
    )
  }
}
