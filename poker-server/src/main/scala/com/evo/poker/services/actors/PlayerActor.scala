package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}

import scala.concurrent.duration._

import com.evo.poker.logic.{GameTransition, PlayerTransition}
import com.evo.poker.services.Services
import com.evo.poker.services.actors.PlayerActor._
import com.evo.poker.services.models.{ActiveGame, ErrorMessage, GameProjection}

class PlayerActor(val playerId: String) extends Actor with Timers {
  private val actorService = Services.actor
  private val lobbyActor   = actorService.lobbyActor

  private var balance: Int = 5000

  private var currentGames: Map[String, GameProjection] = Map.empty
  private var currentGameActors: Map[String, ActorRef]  = Map.empty

  private var connections: Map[String, ActorRef] = Map.empty[String, ActorRef]

  actorService.subscribe(self, classOf[GameActor.GameStateChanged])
  actorService.subscribe(self, classOf[LobbyActor.ActiveGamesState])

  timers.startTimerWithFixedDelay("ping-timer", PingClients, 10.seconds)

  lobbyActor ! LobbyActor.PublishActiveGames()

  override def receive: Receive = {
    case Connected(connectionId, connectionRef) =>
      println(s"Player [$playerId]: New connection [$connectionId]")
      connections += connectionId -> connectionRef
      connectionRef ! PlayerState(balance, currentGames)

    case Disconnected(connectionId) =>
      println(s"Player [$playerId]: Disconnected [$connectionId]")
      connections -= connectionId

    case ConnectionEvent(connectionId, clientEvent) =>
      handleClientEvent(connectionId, clientEvent)

    case PingClients =>
      broadcastMessage(Ping)

    case LobbyActor.ActiveGamesState(activeGames) =>
      broadcastMessage(ActiveGamesState(activeGames))

    case GameActor.GameStateChanged(gameRef, gameId, game) =>
      if (game.players.exists(_.id == playerId)) {
        currentGames += gameId -> GameProjection.of(playerId, game)
        currentGameActors += gameId -> gameRef
        broadcastMessage(GameState(gameId, GameProjection.of(playerId, game)))
      } else if (currentGames.contains(gameId)) {
        // TODO: remove games properly (update balance, etc...)
        currentGames -= gameId
        broadcastMessage(GameState(gameId, GameProjection.of(playerId, game)))
      }

    case GameActor.GameTransitionError(_, _, error, correlationData) =>
      correlationData match {
        case connectionId: String =>
          connections.get(connectionId).foreach { _ ! ErrorMessage(error) }
      }
  }

  private def handleClientEvent(connectionId: String, clientEvent: ClientEvent): Unit = {
    clientEvent match {
      case BuyChipsCommand(amount) =>
        ???

      case CreateGameCommand(name, smallBlind, buyIn) =>
        if (balance >= buyIn) {
          lobbyActor ! LobbyActor.CreateGame(playerId, name, smallBlind, buyIn, connectionId)
          balance -= buyIn
          broadcastMessage(PlayerState(balance, currentGames))
        } else {
          connections.get(connectionId).foreach {
            _ ! ErrorMessage("Not enough money to create a game with that buy-in value")
          }
        }

      case TransitionCommand(gameId, gt) => {
        currentGameActors.get(gameId).foreach { gameActor =>
          gt match {
            case p: PlayerTransition if p.playerId == playerId =>
              gameActor ! GameActor.TransitionCommand(gt, connectionId)
            case _ =>
          }
        }
      }

      case _ => // noop
    }
  }

  def broadcastMessage(message: ServerEvent): Unit = {
    connections.values.foreach(_ ! message)
  }
}

object PlayerActor {

  sealed trait MessageIn
  case class Connected(connectionId: String, connectionRef: ActorRef)  extends MessageIn
  case class Disconnected(connectionId: String)                        extends MessageIn
  case class ConnectionEvent(connectionId: String, event: ClientEvent) extends MessageIn
  case object PingClients                                              extends MessageIn

  sealed trait ClientEvent
  case object Pong                                                        extends ClientEvent
  case class BuyChipsCommand(amount: Int)                                 extends ClientEvent
  case class CreateGameCommand(name: String, smallBlind: Int, buyIn: Int) extends ClientEvent
  case class TransitionCommand(gameId: String, gt: GameTransition)        extends ClientEvent

  sealed trait ServerEvent
  case object Ping                                                         extends ServerEvent
  case class PlayerState(balance: Int, games: Map[String, GameProjection]) extends ServerEvent
  case class ActiveGamesState(activeGames: Vector[ActiveGame])             extends ServerEvent
  case class GameState(gameId: String, game: GameProjection)               extends ServerEvent
}
