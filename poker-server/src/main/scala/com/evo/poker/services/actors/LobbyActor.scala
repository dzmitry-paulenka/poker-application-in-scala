package com.evo.poker.services.actors

import java.util.UUID

import akka.actor.{Actor, PoisonPill, Props}

import com.evo.poker.logic.{Ended, Join}
import com.evo.poker.services.Services
import com.evo.poker.services.actors.LobbyActor.{ActiveGamesState, CreateGame, PublishActiveGames}
import com.evo.poker.services.models.ActiveGame

class LobbyActor extends Actor {
  private val actorService = Services.actor
  private val actorSystem  = actorService.system

  private var activeGames: Vector[ActiveGame] = Vector()

  actorService.subscribe(self, classOf[GameActor.GameStateChanged])

  override def receive: Receive = {
    case PublishActiveGames() =>
      publishActiveGames()

    case CreateGame(playerId, name, smallBlind, buyIn, correlationData) =>
      val gameId = UUID.randomUUID().toString

      val gameRef = actorSystem.actorOf(Props(classOf[GameActor], gameId, name, smallBlind, buyIn))
      gameRef ! GameActor.TransitionCommand(Join(playerId), correlationData)

      activeGames :+= ActiveGame(gameId, name, smallBlind, buyIn, 1)
      publishActiveGames()

    case GameActor.GameStateChanged(gameRef, gameId, game) =>
      val index = activeGames.indexWhere(_.id == gameId)
      if (index >= 0) {
        if (game.phase == Ended) {
          activeGames = activeGames.filterNot(_.id == gameId)
          gameRef ! PoisonPill
        } else {
          val activeGame = activeGames(index).copy(playersCount = game.players.size)
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
  case class PublishActiveGames() extends MessageIn
  case class CreateGame(playerId: String, name: String, smallBlind: Int, buyIn: Int, correlationData: Any)
      extends MessageIn

  sealed trait Event
  case class ActiveGamesState(activeGames: Vector[ActiveGame]) extends Event
}
