package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef}

import com.evo.poker.logic.{Deck, Game, GameTransition, Rules}
import com.evo.poker.services.Services
import com.evo.poker.services.actors.GameActor.{GameStateChanged, GameTransitionError, TransitionCommand}

class GameActor(val gameId: String, val name: String, val smallBlind: Int, val buyIn: Int) extends Actor {
  private val actorService = Services.actor

  var game: Game = Game.create(Rules.texas(smallBlind, buyIn), Deck.random())

  override def receive: Receive = {
    case TransitionCommand(gt, correlationData) =>
      game
        .transition(gt)
        .swap
        .fold(
          ng => {
            game = ng
            actorService.publish(
              GameStateChanged(self, gameId, game)
            )
          },
          error => {
            actorService.publish(
              GameTransitionError(self, gameId, error, correlationData)
            )
          }
        )
  }
}

object GameActor {
  sealed trait MessageIn
  case class TransitionCommand(gt: GameTransition, correlationData: Any) extends MessageIn

  sealed trait Event
  case class GameStateChanged(gameRef: ActorRef, gameId: String, game: Game) extends Event
  case class GameTransitionError(
    gameRef: ActorRef,
    gameId: String,
    error: String,
    correlationData: Any
  ) extends Event
}
