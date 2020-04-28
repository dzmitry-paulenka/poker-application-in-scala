package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.Services
import com.evo.poker.services.actors.GameActor.{AutoTransition, GameTransitionError, GameTransitioned, TransitionCommand}

class GameActor(val gameId: String, val name: String, val smallBlind: Int, val buyIn: Int) extends Actor with Timers {
  private val actorService = Services.actor

  private var game: Game = Game.create(Rules.texas(smallBlind, buyIn), Deck.random(), name)

  this.rescheduleAutoTransition()

  override def receive: Receive = {
    case AutoTransition =>
      if (game.canDeal)
        self ! TransitionCommand(Deal, "auto-transition")
      else if (game.canNextRound)
        self ! TransitionCommand(NextRound, "auto-transition")

    case TransitionCommand(transition, correlationData) =>
      game
        .transition(transition)
        .swap
        .fold(
          newGame => {
            actorService.publish(
              GameTransitioned(self, gameId, transition, game, newGame)
            )
            game = newGame
            rescheduleAutoTransition()
          },
          error => {
            actorService.publish(
              GameTransitionError(self, gameId, error, correlationData)
            )
          }
        )
  }

  private def rescheduleAutoTransition(): Unit = {
    timers.startTimerWithFixedDelay("auto-transition-timer", AutoTransition, 3.seconds)
  }
}

object GameActor {
  sealed trait MessageIn
  case object AutoTransition
  case class TransitionCommand(gt: GameTransition, correlationData: Any) extends MessageIn

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
    correlationData: Any
  ) extends Event
}
