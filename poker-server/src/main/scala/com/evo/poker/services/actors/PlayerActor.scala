package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}
import cats._
import cats.instances.all._
import cats.syntax.all._

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.actors.PlayerActor._
import com.evo.poker.services.models.{ActiveGame, GameProjection}

class PlayerActor(actorService: ActorService, val playerId: String) extends Actor with Timers {
  private val lobbyActor = actorService.lobbyActor

  private var balance: Int = 5000

  private var currentGames: Vector[GameProjection] = Vector.empty

  private var connections: Map[String, ActorRef] = Map.empty[String, ActorRef]

  actorService.subscribe(self, classOf[GameActor.GameTransitioned])
  actorService.subscribe(self, classOf[GameActor.GameTransitionError])
  actorService.subscribe(self, classOf[LobbyActor.ActiveGamesState])

  timers.startTimerWithFixedDelay("ping-timer", PingClients, 10.seconds)

  override def receive: Receive = {
    case Connected(connectionId, connectionRef) =>
      println(s"Player [$playerId]: New connection [$connectionId]")
      connections += connectionId -> connectionRef
      lobbyActor ! LobbyActor.PublishActiveGames()
      broadcastPlayerState()

    case Disconnected(connectionId) =>
      println(s"Player [$playerId]: Disconnected [$connectionId]")
      connections -= connectionId
      broadcastPlayerState()

    case ConnectionEvent(connectionId, clientEvent) =>
      handleClientEvent(connectionId, clientEvent)

    case PingClients =>
      broadcastMessage(Ping)

    case LeaveAllGamesIfNoConnections =>
      leaveAllGamesIfNoConnections()

    case LobbyActor.ActiveGamesState(activeGames) =>
      broadcastMessage(ActiveGamesState(activeGames))

    case GameActor.GameTransitioned(_, gameId, transition, prevGame, game) =>
      val gameIndex      = currentGames.indexWhere(_.id == gameId)
      val gameProjection = GameProjection.of(playerId, gameId, game)

      transition match {
        case Join(gPlayerId) if gPlayerId == playerId =>
          game
            .players
            .find(_.id == playerId)
            .foreach(p => balance -= p.balance)
        case Leave(gPlayerId) if gPlayerId == playerId =>
          prevGame
            .players
            .find(_.id == playerId)
            .foreach(p => balance += p.balance + p.resultMoneyWon)
        case _ =>
      }

      if (game.players.exists(_.id == playerId)) {
        if (gameIndex >= 0) {
          currentGames = currentGames.updated(gameIndex, gameProjection);
        } else {
          currentGames :+= gameProjection
        }
      } else if (gameIndex >= 0) {
        currentGames = currentGames.filterNot(_.id == gameId)
      }
      broadcastPlayerState()

    case GameActor.GameTransitionError(_, _, error, correlationData) =>
      println("Got game transition error: " + error)
      correlationData match {
        case connectionId: String =>
          connections.get(connectionId).foreach { _ ! ErrorMessage(error) }
      }
  }

  private def handleClientEvent(connectionId: String, clientEvent: ClientEvent): Unit = {
    clientEvent match {
      case BuyChipsCommand(amount) =>
        balance += amount
        broadcastPlayerState()

      case CreateGameCommand(name, smallBlind, buyIn) =>
        if (balance >= buyIn) {
          lobbyActor ! LobbyActor.CreateGame(playerId, name, smallBlind, buyIn, connectionId)
        } else {
          connections.get(connectionId).foreach {
            _ ! ErrorMessage("Not enough money to create a game with that buy-in value")
          }
        }

      case TransitionCommand(gameId, gt) => {
        actorService.gameActor(gameId).foreach { gameActor =>
          gt match {
            case NextRound =>
              gameActor ! GameActor.TransitionCommand(gt, connectionId)
            case p: PlayerTransition if p.playerId == playerId =>
              gameActor ! GameActor.TransitionCommand(gt, connectionId)
            case _ =>
          }
        }
      }

      case _ => // noop
    }
  }

  private def broadcastPlayerState(): Unit = {
    broadcastMessage(PlayerState(playerId, balance, currentGames))
    timers.startSingleTimer("leave-games", LeaveAllGamesIfNoConnections, 5.seconds)
  }

  private def leaveAllGamesIfNoConnections(): Unit = {
    if (connections.isEmpty) {
      currentGames.foreach { game =>
        actorService.gameActor(game.id).foreach { gameActor =>
          gameActor ! GameActor.TransitionCommand(Leave(playerId), "connections-closed")
        }
      }
    }
  }

  private def broadcastMessage(message: ServerEvent): Unit =
    connections.values.foreach(_ ! message)
}

object PlayerActor {

  sealed trait MessageIn
  case class Connected(connectionId: String, connectionRef: ActorRef)  extends MessageIn
  case class Disconnected(connectionId: String)                        extends MessageIn
  case class ConnectionEvent(connectionId: String, event: ClientEvent) extends MessageIn
  case object PingClients                                              extends MessageIn
  case object LeaveAllGamesIfNoConnections                             extends MessageIn

  sealed trait ClientEvent
  case object Pong                                                         extends ClientEvent
  case class BuyChipsCommand(amount: Int)                                  extends ClientEvent
  case class CreateGameCommand(name: String, smallBlind: Int, buyIn: Int)  extends ClientEvent
  case class TransitionCommand(gameId: String, transition: GameTransition) extends ClientEvent

  sealed trait ServerEvent
  case object Ping                                                                extends ServerEvent
  case class PlayerState(id: String, balance: Int, games: Vector[GameProjection]) extends ServerEvent
  case class ActiveGamesState(activeGames: Vector[ActiveGame])                    extends ServerEvent
  case class GameState(gameId: String, game: GameProjection)                      extends ServerEvent
  case class ErrorMessage(error: String)                                          extends ServerEvent
}
