package com.evo.poker.services.actors.bots

import cats._
import cats.instances.all._
import cats.syntax.all._

import com.evo.poker.logic._

object BotBrain {
  type BotBrain = (String, Game) => Option[GameTransition]

  val caller: BotBrain =
    (botId, game) => {
      if (game.check(botId).isRight)
        Check(botId).some
      else if (game.call(botId).isRight)
        Call(botId).some
      else
        Fold(botId).some
    }

  val aggressor: BotBrain =
    BotBrain.caller

  val smart: BotBrain =
    BotBrain.caller

  def of(botType: String): BotBrain =
    botType.toLowerCase match {
      case "aggressor" => aggressor
      case "smart"     => smart
      case _           => caller
    }
}
