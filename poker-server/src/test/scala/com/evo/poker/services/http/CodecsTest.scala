package com.evo.poker.services.http

import cats._
import cats.instances.all._
import cats.syntax.all._
import io.circe.Encoder
import io.circe.parser._
import io.circe.syntax._
import org.scalatest.flatspec.AnyFlatSpec

import com.evo.poker.GameTestHelper
import com.evo.poker.logic._
import com.evo.poker.services.actors.PlayerActor.{Ping, ServerEvent}
import com.evo.poker.services.http.Codecs._
import com.evo.poker.services.models.GameProjection

class CodecsTest extends AnyFlatSpec with GameTestHelper {

  it should "correctly serialize to json" in {
    assertJson[ServerEvent](Ping, """{"eventType": "ping"}""")
    assertJson[Phase](PreFlop, """ "pre-flop" """)
    assertJson[Card](Card(Rank.Ace, Suit.Hearts), """ {"rank": "A", "suit": "h"} """)
    assertJson[GameTransition](Join("id1"), """ {"transition": "join", "playerId": "id1"} """)
  }

  it should "correctly serialize game to json" in {
    val game = sampleGame(2, "Ac7c Qs9h")
      .flatMap(_.deal())
      .right
      .value

    val projection = GameProjection.of("a", "game1", game)
    assertJson[GameProjection](
      projection,
      """
        {
          "id" : "game1",
          "name" : "noname",
          "phase" : "pre-flop",
          "board" : [],
          "pot" : 3,
          "smallBlind" : 1,
          "roundBet" : 2,
          "players" : [
            {
              "id" : "a",
              "balance" : 98,
              "hand" : [
                {
                  "rank" : "A",
                  "suit" : "c"
                },
                {
                  "rank" : "7",
                  "suit" : "c"
                }
              ],
              "gameBet" : 2,
              "roundBet" : 2,
              "resultComboName" : null,
              "resultMoneyWon" : 0,
              "actedInRound" : false,
              "allIn" : false,
              "sittingOut" : false
            },
            {
              "id" : "b",
              "balance" : 99,
              "hand" : [
              ],
              "gameBet" : 1,
              "roundBet" : 1,
              "resultComboName" : null,
              "resultMoneyWon" : 0,
              "actedInRound" : false,
              "allIn" : false,
              "sittingOut" : false
            }
          ],
          "currentPlayerIndex" : 1,
          "dealerPlayerIndex" : 0
        }
        """
    )
  }

  def assertJson[T](message: T, json: String)(implicit encoder: Encoder[T]): Unit = {
    message.asJson shouldBe parse(json).right.value
  }
}
