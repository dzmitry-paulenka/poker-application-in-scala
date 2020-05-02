package com.evo.poker.parser

import atto.Atto._
import atto.Parser.{Failure, State, Success, TResult}
import atto._
import cats._
import cats.instances.all._
import cats.syntax.all._

import com.evo.poker.logic.Card.{Board, CardList, Hand}
import com.evo.poker.logic.{Card, Rank, Rules, Suit}

sealed abstract case class PokerParser private (private val rules: Rules) {
  private val rank: Parser[Rank] =
    oneChar("rank")(Rank.fromChar)

  private val suit: Parser[Suit] =
    oneChar("suit")(Suit.fromChar)

  private val card: Parser[Card] =
    (rank, suit).mapN(Card(_, _))

  private val hand: Parser[Hand] = cardList(rules.handSize, "hand")

  private val board: Parser[Board] = cardList(rules.boardSize, "board")

//  private val game: Parser[Game] =
//    (whitespaces ~> board <~ whitespaces1, hand.sepBy1(whitespaces1) <~ whitespaces <~ eol).mapN { (b, hs) =>
//      logic.Game(rules, b, hs.toList)
//    }

  private def cardList(n: Int, name: String): Parser[CardList] =
    card.manyN(n) |? { nestedErr =>
      err[CardList](s"Error parsing $name: $nestedErr")
    }

  private def whitespaces: Parser[Unit] =
    many(horizontalWhitespace).void

  private def whitespaces1: Parser[Unit] =
    many1(horizontalWhitespace).void

  private def oneChar[A](thing: String)(mapper: Char => Option[A]): Parser[A] = {
    elem(_ => true, thing).flatMap { ch =>
      mapper(ch) match {
        case Some(r) => ok(r)
        case None    => pos.flatMap(chPos => err[A](s"Invalid $thing '$ch' at position: $chPos"))
      }
    }
  }

  private def eol: Parser[Unit] =
    endOfInput | pos.flatMap { pos =>
      err(s"End of line is expected on position $pos")
    }

  implicit class MoreParserOps[A](parser: Parser[A]) {
    // a rip-of from `||` but with an ability to use existing error message
    def |?[B >: A](f: String => Parser[B]): Parser[B] = {
      new Parser[B] {
        def apply[R](st0: State, kf: Failure[R], ks: Success[B, R]): TResult[R] =
          Eval.defer(
            parser(
              st0,
              (st1: State, _: List[String], err: String) => f(err)(st1.copy(pos = st0.pos), kf, ks),
              ks
            )
          )
      }
    }
  }

  def parseCard(stringCard: String): Either[String, Card] =
    card.parseOnly(stringCard).either

  def parseCards(stringCards: String, requireSize: Int = -1): Either[String, CardList] = {
    val sCards = stringCards.replaceAll(" ", "")
    val nCards = if (requireSize < 0) sCards.length / 2 else requireSize

    cardList(nCards, "cards")
      .parseOnly(sCards)
      .either
  }

  //  def parseGame(stringGame: String): Either[String, Game] =
//    game.parseOnly(stringGame).either
}

object PokerParser {
  def of(rules: Rules): PokerParser =
    new PokerParser(rules) {}
}
