package com.evo.poker.services.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream._

import scala.concurrent.ExecutionContext

import com.evo.poker.logic.Rules

class ActorService(system: ActorSystem, ec: ExecutionContext, mat: Materializer) {

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
    // todo: not thread-safe
    println(s"Creating new game actor for $gameId: name: $name")
    val ref = system.actorOf(Props(classOf[GameActor], this, gameId, name, Rules.texas(smallBlind, buyIn)))
    gameActors += gameId -> ref
    ref
  }

  def gameActor(gameId: String): Option[ActorRef] = {
    gameActors.get(gameId)
  }

  def playerActor(playerId: String): ActorRef = {
    // todo: not thread-safe
    playerActors.get(playerId) match {
      case Some(ref) => ref
      case None => {
        println(s"Creating new actor for $playerId")
        val ref = system.actorOf(Props(classOf[PlayerActor], this, playerId))
        playerActors += playerId -> ref
        ref
      }
    }
  }
}
