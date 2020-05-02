package com.evo.poker.services.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream._
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext

import com.evo.poker.logic.Rules
import com.evo.poker.services.db.{UserEntity, UserRepository}

class ActorService(repository: UserRepository, system: ActorSystem, ec: ExecutionContext, mat: Materializer) {
  private val logger = Logger[ActorService]

  private var playerActors: Map[String, ActorRef] = Map.empty[String, ActorRef]
  private var gameActors: Map[String, ActorRef]   = Map.empty[String, ActorRef]

  var lobbyActor: ActorRef = system.actorOf(Props(classOf[LobbyActor], this))

  def stop(): Unit = {
    system.terminate()
  }

  def subscribe(subscriber: ActorRef, eventClass: Class[_]): Boolean = {
    system.eventStream.subscribe(subscriber, eventClass)
  }

  def publish(event: Any): Unit = {
    system.eventStream.publish(event)
  }

  def createGameActor(gameId: String, name: String, smallBlind: Int, buyIn: Int): ActorRef = {
    logger.info(s"Creating new game actor for Game(name: $name, id: $gameId)")
    val ref = system.actorOf(Props(classOf[GameActor], this, gameId, name, Rules.texas(smallBlind, buyIn)))
    gameActors += gameId -> ref
    ref
  }

  def gameActor(gameId: String): Option[ActorRef] = {
    gameActors.get(gameId)
  }

  def playerActor(user: UserEntity): ActorRef = {
    // todo: not thread-safe
    val userId = user._id.toHexString
    playerActors.get(userId) match {
      case Some(ref) => ref
      case None => {
        val username = user.username
        val balance  = user.balance
        logger.info(s"Creating new player actor for User(id: $userId, username: $username, balance: $balance)")

        val ref = system.actorOf(Props(classOf[PlayerActor], this, repository, user))
        playerActors += userId -> ref
        ref
      }
    }
  }
}
