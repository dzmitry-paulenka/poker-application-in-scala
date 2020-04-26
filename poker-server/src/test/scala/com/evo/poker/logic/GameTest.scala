package com.evo.poker.logic

import cats._
import cats.instances.all._
import cats.syntax.all._
import org.scalatest._

import com.evo.poker.GameTestHelper

//noinspection ScalaDeprecation
class GameTest extends FlatSpec with GameTestHelper {
  val sb = rules.smallBlind
  val bb = rules.bigBlind

  implicit class UnsafeGameOpts(g: Game) {
    def player(id: String): Player =
      g.requirePlayer(id).right.value
  }

  it should "start correctly" in {
    assertValidGame {
      for {
        g <- sampleGame(3)
        g <- g.deal()
        g <- validate(g) { g =>
          g.phase shouldBe PreFlop
          g.players.size shouldBe 3

          g.player("a") shouldBe g.currentPlayer
          g.player("b").balance shouldBe (100 - sb)
          g.player("c").balance shouldBe (100 - bb)

          g.pot shouldBe (sb + bb)
          g.roundBet shouldBe bb
        }
      } yield g
    }
  }

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

        g <- g.nextRound()
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
}
