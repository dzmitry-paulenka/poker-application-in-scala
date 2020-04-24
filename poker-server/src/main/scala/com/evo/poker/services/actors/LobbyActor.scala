package com.evo.poker.services.actors

import akka.actor.Actor

class LobbyActor() extends Actor {

  override def receive: Receive = {
    ???
  }

}

object LobbyActor {
  sealed trait MessageIn
  case class CreateGame(playerId: String, name: String, smallBlind: Int, buyIn: Int) extends MessageIn
}
