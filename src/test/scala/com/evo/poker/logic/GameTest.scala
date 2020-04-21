package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._
import org.scalatest._
import org.scalatest.matchers.should.Matchers

import com.evo.poker.logic.Card.CardList
import com.evo.poker.parser.PokerParser

//noinspection ScalaDeprecation
class GameTest extends FlatSpec with Matchers with EitherValues {
  val rules  = Rules.Texas
  val parser = PokerParser.of(rules)

  val sb = rules.smallBlind
  val bb = rules.bigBlind

//  it should "start correctly" in {
//    assertValidGame {
//      for {
//        g <- sampleGame(3)
//        g <- g.deal()
//        g <- validate(g) { g =>
//          g.phase shouldBe PreFlop
//          g.players.size shouldBe 3
//
//          g.player("a") shouldBe g.currentPlayer
//          g.player("b").balance shouldBe (100 - sb)
//          g.player("c").balance shouldBe (100 - bb)
//
//          g.pot shouldBe (sb + bb)
//          g.roundBet shouldBe bb
//        }
//      } yield g
//    }
//  }

  it should "play out correctly 1" in {
    assertValidGame {
      for {
        g <- sampleGame(4, "Ac7c Qs9h KsJh 2h3h 9cKc3h5s9d")
        g <- g.deal()
        g <- validate(g) { g =>
          g.currentPlayer.id shouldBe "d"
          g.check("d").left.value should include("can't check")
        }
        g <- g.fold("d")

        g <- g.call("a")
        g <- g.call("b")
        g <- g.check("c")
        g <- validate(g) { g =>
          g.phase shouldBe Flop
          g.pot shouldBe 6
          g.board shouldBe cards("9cKc3h")

          g.activePlayers.length shouldBe 3
          g.player("a").gameBet shouldBe 2
          g.player("b").gameBet shouldBe 2
          g.player("c").gameBet shouldBe 2
          g.player("d").sittingOut shouldBe true
          g.currentPlayer.id shouldBe "b"

          g.check("a").left.value should include("out of turn")
        }

        g <- g.check("b")
        g <- g.raise("c", 2)
        g <- g.raise("a", 2)
        g <- g.call("b")
        g <- g.call("c")
        g <- validate(g) { g =>
          g.phase shouldBe Turn
          g.pot shouldBe 18
          g.board shouldBe cards("9cKc3h5s")

          g.activePlayers.length shouldBe 3
          g.player("a").gameBet shouldBe 6
          g.player("b").gameBet shouldBe 6
          g.player("c").gameBet shouldBe 6
          g.currentPlayer.id shouldBe "b"
        }

        g <- g.check("b")
        g <- g.check("c")
        g <- g.check("a")
        g <- validate(g) { g =>
          g.phase shouldBe River
          g.pot shouldBe 18
          g.board shouldBe cards("9cKc3h5s9d")

          g.activePlayers.length shouldBe 3
          g.player("a").gameBet shouldBe 6
          g.player("b").gameBet shouldBe 6
          g.player("c").gameBet shouldBe 6
          g.currentPlayer.id shouldBe "b"
        }

        g <- g.raise("b", 4)
        g <- g.call("c")
        g <- g.fold("a")
        g <- validate(g) { g =>
          g.phase shouldBe Showdown
          g.pot shouldBe 26
          g.board shouldBe cards("9cKc3h5s9d")

          g.activePlayers.length shouldBe 2
          g.player("b").gameBet shouldBe 10
          g.player("b").resultMoneyWon shouldBe 26

          g.player("c").gameBet shouldBe 10

          g.player("a").sittingOut shouldBe true

          g.currentPlayerIndex shouldBe -1
        }

        g <- g.finish()
        g <- g.leave("a")
        g <- validate(g) { g =>
          g.phase shouldBe PreDeal
          g.pot shouldBe 0
          g.board shouldBe empty
          g.players.length shouldBe 3
          g.currentPlayerIndex shouldBe -1

          g.player("b").balance shouldBe 116
          g.player("c").balance shouldBe 90
          g.player("d").balance shouldBe 100

        }
      } yield g
    }
  }

  it should "detect errors" in {
    assertErrorGame("deal") {
      for {
        g <- sampleGame(3)
        g <- g.deal()
        g <- g.deal()
      } yield g
    }
  }

  it should "handle join/leave properly" in {}

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
      (0 until playerCnt).map(i => g => g.join(('a' + i).toChar.toString, 100))
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
