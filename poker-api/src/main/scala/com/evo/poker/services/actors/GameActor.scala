package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.actors.GameActor.{AutoTransition, GameTransitionErrorEvent, GameTransitionedEvent, TransitionCommand}

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
              GameTransitionedEvent(self, gameId, transition, game, newGame)
            )
            game = newGame
          },
          error => {
            actorService.publish(
              GameTransitionErrorEvent(self, gameId, error, correlationKey)
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
