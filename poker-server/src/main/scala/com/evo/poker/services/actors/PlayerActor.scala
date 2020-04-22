package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef}

import com.evo.poker.logic.GameTransition
import com.evo.poker.services.Services
import com.evo.poker.services.actors.PlayerActor._

class PlayerActor(val playerName: String) extends Actor {
  private val actorService = Services.actor

  private var balance: Int = 5000

  // TODO: use this
  private var activeGames = ???

  private var connections: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case Connected(connectionId, connectionRef) =>
      println(s"[$playerName]: Connected [$connectionId]")
      connections += connectionId -> connectionRef

    case Disconnected(connectionId) =>
      println(s"[$playerName]: Disconnected [$connectionId]")
      connections -= connectionId

    case ConnectionMessage(connectionId, clientMessage) =>
      clientMessage match {
        case ClientDataMessage(data) =>
          println(s"[$playerName]: Conn[$connectionId]: $data")
          broadcastMessage(ServerDataMessage(data))

        case msg @ GetGameList() =>
          actorService.lobbyActor ! msg

        case msg @ CreateGame(smallBlind) =>
          actorService.lobbyActor ! msg

        case Transition(gameId, gt) =>
          ???

        case _ => // noop
      }
  }

  def broadcastMessage(message: ServerMessage) = {
    connections.values.foreach(_ ! message)
  }
}

object PlayerActor {

  sealed trait MessageIn
  case class Connected(connectionId: String, connectionRef: ActorRef)        extends MessageIn
  case class Disconnected(connectionId: String)                              extends MessageIn
  case class ConnectionMessage(connectionId: String, message: ClientMessage) extends MessageIn

  sealed trait ServerMessage
  case class Ping(msg: String)               extends ServerMessage
  case class ServerDataMessage(data: String) extends ServerMessage

  sealed trait ClientMessage
  case class Pong(msg: String)                              extends ClientMessage
  case class GetGameList()                                  extends ClientMessage
  case class CreateGame(smallBlind: Int)                    extends ClientMessage
  case class Transition(gameId: String, gt: GameTransition) extends ClientMessage

  case class ClientDataMessage(data: String) extends ClientMessage
}
