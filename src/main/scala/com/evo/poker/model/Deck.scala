package com.evo.poker.model

import cats._
import cats.instances.all._
import cats.syntax.all._
import com.evo.poker.model.Card.CardList

import scala.util.Random

sealed abstract class Deck() {
  def reset(): Deck

  def deal(n: Int): (CardList, Deck)

  def deal1(): (Card, Deck) =
    deal(1).leftMap(_.head)
}

class RandomDeck(cards: List[Card], resetSeed: Long) extends Deck {

  override def reset(): Deck =
    Deck.random(resetSeed);

  override def deal(n: Int): (CardList, Deck) = {
    (cards.take(n), new RandomDeck(cards.drop(n), resetSeed))
  }
}

class CardListDeck(cards: List[Card]) extends Deck {

  override def reset(): Deck = this

  override def deal(n: Int): (CardList, Deck) = {
    (cards.take(n), new CardListDeck(cards.drop(n)))
  }
}

object Deck {
  private val rand = new Random()

  def of(cards: List[Card]): Deck = {
    new CardListDeck(cards)
  }

  def random(): Deck =
    random(System.nanoTime())

  def random(seed: Long): Deck = {
    val random = new Random(seed)
    val cards = random.shuffle(Card.all)
    val resetSeed = random.nextLong()

    new RandomDeck(
      cards,
      resetSeed
    )
  }
}
