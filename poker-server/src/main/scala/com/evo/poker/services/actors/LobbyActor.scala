package com.evo.poker.services.actors

import java.util.UUID

import akka.actor.{Actor, PoisonPill}

import com.evo.poker.logic.{Ended, Join}
import com.evo.poker.services.actors.LobbyActor.{ActiveGamesState, CreateGame, PublishActiveGames}
import com.evo.poker.services.models.ActiveGame

class LobbyActor(actorService: ActorService) extends Actor {
  private var activeGames: Vector[ActiveGame] = Vector()

  actorService.subscribe(self, classOf[GameActor.GameTransitioned])

  override def receive: Receive = {
    case PublishActiveGames() =>
      publishActiveGames()

    case CreateGame(playerId, name, smallBlind, buyIn, correlationData) =>
      val gameId = UUID.randomUUID().toString

      val gameRef = actorService.createGameActor(gameId, name, smallBlind, buyIn)
      gameRef ! GameActor.TransitionCommand(Join(playerId), correlationData)

      activeGames :+= ActiveGame(gameId, name, smallBlind, buyIn, 1)
      publishActiveGames()

    case GameActor.GameTransitioned(gameRef, gameId, _, _, game) =>
      val index = activeGames.indexWhere(_.id == gameId)
      if (index >= 0) {
        if (game.players.isEmpty || game.phase == Ended) {
          activeGames = activeGames.filterNot(_.id == gameId)
          gameRef ! PoisonPill
        } else {
          val activeGame = activeGames(index).copy(playerCount = game.players.size)
          activeGames = activeGames.updated(index, activeGame)
        }

        publishActiveGames()
      }
  }

  private def publishActiveGames(): Unit = {
    actorService.publish(ActiveGamesState(activeGames))
  }
}

object LobbyActor {
  sealed trait MessageIn
  case class PublishActiveGames()                                                                          extends MessageIn
  case class CreateGame(playerId: String, name: String, smallBlind: Int, buyIn: Int, correlationData: Any) extends MessageIn

  sealed trait Event
  case class ActiveGamesState(activeGames: Vector[ActiveGame]) extends Event
}
