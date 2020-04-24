package com.evo.poker.services.actors

import akka.actor.Actor

import com.evo.poker.logic.{Deck, Game, GameTransition, Rules}

class GameActor(val gameId: String, val smallBlind: Int) extends Actor {

  var game: Game = Game.create(Rules.texas(smallBlind), Deck.random())

  override def receive: Receive = {
    case gt: GameTransition =>
      game
        .transition(gt)
        .swap
        .fold(
          game = _,
          sender ! ErrorMessage(_)
        )

  }
}

object GameActor {
  sealed trait MessageIn
  case class Transition(gt: GameTransition) extends MessageIn
}
