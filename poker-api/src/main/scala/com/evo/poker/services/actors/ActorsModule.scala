package com.evo.poker.services.actors

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.softwaremill.macwire.{Module, wire}

import scala.concurrent.ExecutionContextExecutor

import com.evo.poker.services.db.DbModule

@Module
trait ActorsModule extends DbModule {
  implicit val system: ActorSystem                        = ActorSystem("poker-actor-system")
  implicit val materializer: Materializer                 = Materializer.matFromSystem
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val actorService = wire[ActorService]
}
