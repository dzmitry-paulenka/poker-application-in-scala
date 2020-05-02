package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.actors.GameActor.{AutoTransition, GameTransitionError, GameTransitioned, TransitionCommand}

class GameActor(actorService: ActorService, gameId: String, name: String, rules: Rules) extends Actor with Timers {

  private var game: Game = Game.create(rules, Deck.random(), name)

  this.rescheduleAutoTransition()

  override def receive: Receive = {
    case AutoTransition =>
      doAutoTransition()

    case TransitionCommand(transition, correlationKey) =>
      game
        .transition(transition)
        .swap
        .fold(
          newGame => {
            actorService.publish(
              GameTransitioned(self, gameId, transition, game, newGame)
            )
            game = newGame
          },
          error => {
            actorService.publish(
              GameTransitionError(self, gameId, error, correlationKey)
            )
          }
        )
  }

  private def doAutoTransition(): Unit = {
    if (game.canDeal)
      self ! TransitionCommand(Deal, "auto-transition")
//    else if (game.canNextRound)
//      self ! TransitionCommand(NextRound, "auto-transition")

    rescheduleAutoTransition()
  }

  private def rescheduleAutoTransition(): Unit = {
    timers.startTimerWithFixedDelay("auto-transition-timer", AutoTransition, 3.seconds)
  }
}

object GameActor {
  sealed trait MessageIn
  case object AutoTransition
  case class TransitionCommand(gt: GameTransition, correlationKey: String) extends MessageIn

  sealed trait Event
  case class GameTransitioned(
    gameRef: ActorRef,
    gameId: String,
    transition: GameTransition,
    prevGame: Game,
    game: Game
  ) extends Event

  case class GameTransitionError(
    gameRef: ActorRef,
    gameId: String,
    error: String,
    correlationKey: String
  ) extends Event
}
