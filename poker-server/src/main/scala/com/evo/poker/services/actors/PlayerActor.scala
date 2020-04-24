package com.evo.poker.services.actors

import akka.actor.{Actor, ActorRef}
import com.evo.poker.logic.{Game, GameTransition, PlayerTransition}
import com.evo.poker.services.Services
import com.evo.poker.services.actors.PlayerActor._

class PlayerActor(val playerId: String) extends Actor {
  private val actorService = Services.actor
  private val lobbyActor   = actorService.lobbyActor

  private var balance: Int = 5000

  private var activeGames: Vector[ActiveGame] = Vector()

  private var currentGames = Map.empty[String, Game]

  private var connections: Map[String, ActorRef] = Map.empty[String, ActorRef]

  override def receive: Receive = {
    case Connected(connectionId, connectionRef) =>
      println(s"Player [$playerId]: New connection [$connectionId]")
      connections += connectionId -> connectionRef
      connectionRef ! PlayerState(balance, currentGames, activeGames)

    case Disconnected(connectionId) =>
      println(s"Player [$playerId]: Disconnected [$connectionId]")
      connections -= connectionId

    case GameStateChanged(gameId, game) =>
      if (game.players.exists(_.id == playerId)) {
        currentGames += gameId -> game
        broadcastMessage(GameState(gameId, game))
      } else {
        // TODO: remove games properly (update balance, etc...)
        currentGames -= gameId
        broadcastMessage(GameState(gameId, game))
      }

    case ActiveGamesChanged(activeGames) =>
      this.activeGames = activeGames
      broadcastMessage(ActiveGames(activeGames))

    case ConnectionEvent(connectionId, clientEvent) =>
      clientEvent match {

        case CreateGame(name, smallBlind, buyIn) =>
          lobbyActor ! LobbyActor.CreateGame(playerId, name, smallBlind, buyIn);

        case Transition(gameId, gt) => {
          actorService.gameActor(gameId).foreach { gameActor =>
            gt match {
              case p: PlayerTransition if p.playerId == playerId =>
                gameActor ! GameActor.Transition(gt)
              case _ =>
            }
          }
        }

        case _ => // noop
      }
  }

  def broadcastMessage(message: ServerEvent) = {
    connections.values.foreach(_ ! message)
  }
}

object PlayerActor {

  sealed trait MessageIn
  case class Connected(connectionId: String, connectionRef: ActorRef)  extends MessageIn
  case class Disconnected(connectionId: String)                        extends MessageIn
  case class ConnectionEvent(connectionId: String, event: ClientEvent) extends MessageIn
  case class ActiveGamesChanged(activeGames: Vector[ActiveGame])       extends MessageIn
  case class GameStateChanged(gameId: String, game: Game)              extends MessageIn

  sealed trait ServerEvent
  case class Ping()                                       extends ServerEvent
  case class ActiveGames(activeGames: Vector[ActiveGame]) extends ServerEvent
  case class GameState(gameId: String, game: Game)        extends ServerEvent
  case class PlayerState(
    balance: Int,
    games: Map[String, Game],
    activeGames: Vector[ActiveGame]
  ) extends ServerEvent

  sealed trait ClientEvent
  case class Pong()                                                extends ClientEvent
  case class CreateGame(name: String, smallBlind: Int, buyIn: Int) extends ClientEvent
  case class Transition(gameId: String, gt: GameTransition)        extends ClientEvent
}
