package com.evo.poker.services.actors

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.softwaremill.macwire.{Module, wire}

import scala.concurrent.ExecutionContextExecutor

@Module
trait ActorsModule {
  implicit val system: ActorSystem                        = ActorSystem("poker-actor-system")
  implicit val materializer: Materializer                 = Materializer.matFromSystem
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  val actorService = wire[ActorService]
}
