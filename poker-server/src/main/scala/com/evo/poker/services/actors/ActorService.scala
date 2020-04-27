package com.evo.poker.services.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream._

import scala.concurrent.ExecutionContextExecutor

class ActorService {
  implicit val system: ActorSystem                        = ActorSystem("poker-actor-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer                 = Materializer.matFromSystem

  private var playerActors: Map[String, ActorRef] = Map.empty[String, ActorRef]
  private var gameActors: Map[String, ActorRef]   = Map.empty[String, ActorRef]

  var lobbyActor: ActorRef = _

  def init(): Unit = {
    lobbyActor = system.actorOf(Props(classOf[LobbyActor]))
  }

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
    val ref = system.actorOf(Props(classOf[GameActor], gameId, name, smallBlind, buyIn))
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
        val ref = system.actorOf(Props(classOf[PlayerActor], playerId))
        playerActors += playerId -> ref
        ref
      }
    }
  }
}
