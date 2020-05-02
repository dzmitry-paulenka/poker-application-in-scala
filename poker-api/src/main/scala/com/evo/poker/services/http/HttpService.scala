package com.evo.poker.services.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.typesafe.config.Config
import com.typesafe.scalalogging.Logger

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

import com.evo.poker.services.http.Codecs._

class HttpService(config: Config, routes: Route, implicit val system: ActorSystem, implicit val ec: ExecutionContext) {
  private val logger = Logger[HttpService]

  private var http: Future[Http.ServerBinding] = _

  def start(): Unit = {
    val bindHost = config.getString("http.bindHost")
    val bindPort = config.getInt("http.bindPort")

    http = Http().bindAndHandle(routes, bindHost, bindPort)
    http.onComplete {
      case Success(b) =>
        logger.info(s"Application is up and running at ${b.localAddress.getHostName}:${b.localAddress.getPort}")
      case Failure(e) =>
        logger.error(s"Error starting application: {}", e.getMessage)
    }
  }

  def stop(): Unit = {
    http.flatMap(_.terminate(hardDeadline = 3.seconds)).flatMap { _ =>
      system.terminate()
    }
  }
}
