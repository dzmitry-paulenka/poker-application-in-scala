package com.evo.poker.services.actors

import java.util.concurrent.ConcurrentHashMap

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream._
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext

import com.evo.poker.logic.Rules
import com.evo.poker.services.actors.bots.{BotActor, BotBrain}
import com.evo.poker.services.db.{UserEntity, UserRepository}

class ActorService(repository: UserRepository, system: ActorSystem, ec: ExecutionContext, mat: Materializer) {
  private val logger = Logger[ActorService]

  private val gameActors: ConcurrentHashMap[String, ActorRef]   = new ConcurrentHashMap[String, ActorRef]()
  private val playerActors: ConcurrentHashMap[String, ActorRef] = new ConcurrentHashMap[String, ActorRef]()
  private val botActors: ConcurrentHashMap[String, ActorRef]    = new ConcurrentHashMap[String, ActorRef]()

  val lobbyActor: ActorRef = system.actorOf(Props(classOf[LobbyActor], this))

  def stop(): Unit = {
    system.terminate()
  }

  def subscribe(subscriber: ActorRef, eventClass: Class[_]): Boolean = {
    system.eventStream.subscribe(subscriber, eventClass)
  }

  def publish(event: Any): Unit = {
    system.eventStream.publish(event)
  }

  def ensureGameActor(gameId: String, name: String, smallBlind: Int, buyIn: Int): ActorRef = {
    gameActors.computeIfAbsent(
      gameId,
      _ => {
        logger.info(s"Creating new game actor for Game(name: $name, id: $gameId)")
        system.actorOf(Props(classOf[GameActor], this, gameId, name, Rules.texas(smallBlind, buyIn)))
      }
    )
  }

  def gameActor(gameId: String): Option[ActorRef] = {
    Option(gameActors.get(gameId))
  }

  def ensurePlayerActor(user: UserEntity): ActorRef = {
    val userId = user._id.toHexString
    playerActors.computeIfAbsent(
      userId,
      _ => {
        val username = user.username
        val balance  = user.balance

        logger.info(s"Creating new player actor for User(id: $userId, username: $username, balance: $balance)")
        system.actorOf(Props(classOf[PlayerActor], this, repository, user))
      }
    )
  }

  def ensureBotActor(botId: String, botType: String): ActorRef = {
    botActors.computeIfAbsent(
      botId,
      _ => {
        logger.info(s"Creating new bot actor for (botType: $botType, botId: $botId)")

        val brain = BotBrain.of(botType)
        system.actorOf(Props(classOf[BotActor], this, botId, brain))
      }
    )
  }
}
