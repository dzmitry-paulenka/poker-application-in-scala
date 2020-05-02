package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef, Timers}
import cats._
import cats.instances.all._
import cats.syntax.all._
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._

import com.evo.poker.logic._
import com.evo.poker.services.actors.PlayerActor._
import com.evo.poker.services.db.{UserEntity, UserRepository}
import com.evo.poker.services.models.{ActiveGame, GameProjection}

class PlayerActor(actorService: ActorService, repository: UserRepository, user: UserEntity) extends Actor with Timers {
  private val logger = Logger[PlayerActor]

  private val userId     = user._id.toHexString
  private val playerId   = user.username
  private val lobbyActor = actorService.lobbyActor

  private var balance: Int          = user.balance
  private var blockedBalance: Int   = 0
  private def availableBalance: Int = balance - blockedBalance

  private var currentGames: Vector[GameProjection] = Vector.empty

  private var connections: Map[String, ActorRef] = Map.empty[String, ActorRef]

  actorService.subscribe(self, classOf[GameActor.GameTransitioned])
  actorService.subscribe(self, classOf[GameActor.GameTransitionError])
  actorService.subscribe(self, classOf[LobbyActor.ActiveGamesState])

  timers.startTimerWithFixedDelay("ping-timer", PingClients, 10.seconds)

  override def receive: Receive = {
    case Connected(connectionId, connectionRef) =>
      logger.info(s"User [$playerId]: New connection [$connectionId]")
      connections += connectionId -> connectionRef
      lobbyActor ! LobbyActor.PublishActiveGames()
      broadcastPlayerState()

    case Disconnected(connectionId) =>
      logger.info(s"User [$playerId]: Disconnected [$connectionId]")
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

    case GameActor.GameTransitioned(gameActor, gameId, transition, prevGame, game) =>
      val gameIndex      = currentGames.indexWhere(_.id == gameId)
      val gameProjection = GameProjection.of(playerId, gameId, game)

      transition match {
        case Join(gPlayerId) if gPlayerId == playerId =>
          blockedBalance += game.rules.buyIn
        case Leave(gPlayerId) if gPlayerId == playerId =>
          prevGame
            .players
            .find(_.id == playerId)
            .foreach(p => {
              blockedBalance -= game.rules.buyIn
              addToBalance(p.balance + p.resultMoneyWon - game.rules.buyIn)
            })
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

    case GameActor.GameTransitionError(_, _, error, correlationKey) =>
      logger.error("Got game transition error: " + error)
      connections.get(correlationKey).foreach {
        _ ! ErrorMessage(error)
      }
  }

  private def handleClientEvent(connectionId: String, clientEvent: ClientEvent): Unit = {
    clientEvent match {
      case BuyChipsCommand(amount) =>
        addToBalance(amount)
        broadcastPlayerState()

      case CreateGameCommand(name, smallBlind, buyIn) =>
        lobbyActor ! LobbyActor.CreateGame(playerId, name, smallBlind, buyIn, connectionId)

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

  private def addToBalance(amountToAdd: Int): Unit = {
    balance += amountToAdd

    logger.info(s"Updating user balance: [userId: $userId, name: $playerId, balance = $balance, availableBalance = $availableBalance]")
    repository.updateBalance(userId, balance)
  }

  private def broadcastPlayerState(): Unit = {
    broadcastMessage(PlayerState(playerId, availableBalance, currentGames))
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

  private def broadcastMessage(message: ServerEvent): Unit = {
    connections.values.foreach(_ ! message)
  }
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
