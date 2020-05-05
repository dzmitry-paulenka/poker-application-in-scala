package com.evo.poker.services.actors.bots

import java.util.UUID

import akka.actor.{Actor, ActorRef, Timers}
import cats._
import cats.instances.all._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.actors.bots.BotBrain.BotBrain
import com.evo.poker.services.actors.{ActorService, GameActor}

class BotActor(actorService: ActorService, botId: String, brain: BotBrain) extends Actor with Timers {

  private val logger = Logger[BotActor]

  private final case class SendTransition(gameActor: ActorRef, gt: GameTransition)

  actorService.subscribe(self, classOf[GameActor.GameTransitionedEvent])
  actorService.subscribe(self, classOf[GameActor.GameTransitionErrorEvent])

  override def receive: Receive = {
    case SendTransition(gameActor, gt) =>
      gameActor ! GameActor.TransitionCommand(gt, botId)

    case GameActor.GameTransitionedEvent(gameActor, _, _, _, game) =>
      if (shouldAct(game)) {
        brain(botId, game).foreach { gt =>
          //send with a little delay
          timers.startSingleTimer(UUID.randomUUID().toString, SendTransition(gameActor, gt), 2.seconds)
        }
      }

    case GameActor.GameTransitionErrorEvent(_, _, error, correlationKey) =>
      if (correlationKey == botId) {
        logger.error(s"Bot $botId game transition error: " + error)
      }
  }

  def shouldAct(game: Game): Boolean =
    game.isCurrentPlayer(botId) && Phase.betsAllowed(game.phase)
}
