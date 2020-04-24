package com.evo.poker.services.actors

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.stream._

import scala.concurrent.ExecutionContextExecutor

class ActorService {
  implicit val system: ActorSystem                        = ActorSystem("poker-actor-system")
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: Materializer                 = Materializer.matFromSystem

  var playerHandlers: Map[String, ActorRef] = Map.empty[String, ActorRef]

  def init(): Unit = {
    // TODO: init some actors
    val actorReg = null
  }

  def stop(): Unit = {
    system.terminate()
  }

  // TODO: make this a `val` ?
  def lobbyActor: ActorRef = {
    ???

  }

  def gameActor(gameId: String): Option[ActorRef] = {
    ???
  }

  def playerActor(playerId: String): ActorRef = {
    // todo: not thread-safe
    playerHandlers.get(playerId) match {
      case Some(ref) => ref
      case None => {
        println(s"Creating new actor for $playerId")
        val ref = system.actorOf(Props(classOf[PlayerActor], playerId))
        playerHandlers += playerId -> ref
        ref
      }
    }
  }
}
