package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.actors.GameActor._

class GameActor(actorService: ActorService, gameId: String, name: String, rules: Rules) extends Actor with Timers {

  private var game: Game = Game.create(rules, Deck.random(), name)

  this.rescheduleAutoTransition()

  override def receive: Receive = {
    case AutoTransition =>
      doAutoTransition()

    case AddBot(botType, correlationKey) =>
      if (game.players.size == rules.playersLimit)
        publishError("Can't add bot: too many players", correlationKey)
      else {
        (1 to rules.playersLimit)
          .map(index => s"BOT-[$botType]-$index")
          .find(!game.hasPlayer(_))
          .foreach { botId =>
            actorService.ensureBotActor(botId, botType)
            doTransition(Join(botId), correlationKey)
          }
      }

    case RemoveBot(botId, correlationKey) =>
      if (hasBot(botId)) {
        doTransition(Leave(botId), correlationKey)
      }

    case TransitionCommand(transition, correlationKey) =>
      doTransition(transition, correlationKey)
  }

  private def hasBot(botId: String) = {
    game.player(botId).exists(_.isBot)
  }

  private def publishTransition(gt: GameTransition, prevGame: Game, game: Game) = {
    actorService.publish(
      GameTransitionedEvent(self, gameId, gt, prevGame, game)
    )
  }

  private def publishError(error: String, correlationKey: String): Unit = {
    actorService.publish(
      GameTransitionErrorEvent(self, gameId, error, correlationKey)
    )
  }

  private def doAutoTransition(): Unit = {
    if (game.canDeal)
      doTransition(Deal, "auto-transition")
//    else if (game.canNextRound)
//      doTransition(NextRound, "auto-transition")

    rescheduleAutoTransition()
  }

  private def rescheduleAutoTransition(): Unit = {
    timers.startTimerWithFixedDelay("auto-transition-timer", AutoTransition, 3.seconds)
  }

  private def doTransition(gt: GameTransition, correlationKey: String) =
    game
      .transition(gt)
      .swap
      .fold(
        newGame => {
          publishTransition(gt, game, newGame)
          game = newGame
        },
        error => publishError(error, correlationKey)
      )
}

object GameActor {
  sealed trait MessageIn
  case object AutoTransition
  final case class AddBot(botType: String, correlationKey: String)               extends MessageIn
  final case class RemoveBot(botId: String, correlationKey: String)              extends MessageIn
  final case class TransitionCommand(gt: GameTransition, correlationKey: String) extends MessageIn

  sealed trait MessageOut
  final case class GameTransitionedEvent(
    gameRef: ActorRef,
    gameId: String,
    transition: GameTransition,
    prevGame: Game,
    game: Game
  ) extends MessageOut

  final case class GameTransitionErrorEvent(
    gameRef: ActorRef,
    gameId: String,
    error: String,
    correlationKey: String
  ) extends MessageOut
}
