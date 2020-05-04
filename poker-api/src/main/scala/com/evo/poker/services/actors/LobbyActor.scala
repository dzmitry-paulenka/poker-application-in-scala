package com.evo.poker.services.actors

import java.util.UUID

import akka.actor.{Actor, PoisonPill}

import com.evo.poker.logic.{Ended, Join}
import com.evo.poker.services.actors.LobbyActor.{ActiveGamesStateEvent, CreateGame, PublishActiveGames}
import com.evo.poker.services.models.ActiveGame

class LobbyActor(actorService: ActorService) extends Actor {
  private var activeGames: Vector[ActiveGame] = Vector()

  actorService.subscribe(self, classOf[GameActor.GameTransitionedEvent])

  override def receive: Receive = {
    case PublishActiveGames() =>
      publishActiveGames()

    case CreateGame(playerId, name, smallBlind, buyIn, correlationKey) =>
      val gameId = UUID.randomUUID().toString

      val gameRef = actorService.ensureGameActor(gameId, name, smallBlind, buyIn)
      gameRef ! GameActor.TransitionCommand(Join(playerId), correlationKey)

      activeGames :+= ActiveGame(gameId, name, smallBlind, buyIn, 1)
      publishActiveGames()

    case GameActor.GameTransitionedEvent(gameRef, gameId, _, _, game) =>
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
    actorService.publish(ActiveGamesStateEvent(activeGames))
  }
}

object LobbyActor {
  sealed trait MessageIn
  final case class PublishActiveGames()                                                                            extends MessageIn
  final case class CreateGame(playerId: String, name: String, smallBlind: Int, buyIn: Int, correlationKey: String) extends MessageIn

  sealed trait MessageOut
  final case class ActiveGamesStateEvent(activeGames: Vector[ActiveGame]) extends MessageOut
}
