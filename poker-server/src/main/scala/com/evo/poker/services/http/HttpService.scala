package com.evo.poker.services.http

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.OverflowStrategy
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import io.circe.parser.decode
import io.circe.syntax._
import org.reactivestreams.Publisher

import scala.concurrent.Future
import scala.concurrent.duration._

import com.evo.poker.services.actors.ActorService
import com.evo.poker.services.actors.PlayerActor._
import com.evo.poker.services.http.Codecs._

class HttpService(actorService: ActorService) {
  implicit val system           = actorService.system
  implicit val executionContext = actorService.executionContext
  implicit val materializer     = actorService.materializer

  private var http: Future[Http.ServerBinding] = _

  private val routes = pathPrefix("player-events" / Segment) { playerId =>
    handleWebSocketMessages(
      handlePlayerConnection(playerId)
    )
  }

  def init(): Unit = {
    http = Http().bindAndHandle(routes, "localhost", 8080)
    println("Http server started...")
  }

  def stop(): Unit = {
    http.flatMap(_.terminate(hardDeadline = 3.seconds)).flatMap { _ =>
      system.terminate()
    }
  }

  private def handlePlayerConnection(playerId: String): Flow[Message, Message, Any] = {
    val playerActor: ActorRef = actorService.playerActor(playerId)
    val connectionId: String  = UUID.randomUUID().toString

    val (connectionRef: ActorRef, publisher: Publisher[TextMessage.Strict]) = {
      Source
        .actorRef[ServerEvent](100000, OverflowStrategy.fail)
        .map { message: ServerEvent =>
          TextMessage.Strict(message.asJson.noSpaces)
        }
        .toMat(Sink.asPublisher(false))(Keep.both)
        .run()
    }

    val sink: Sink[Message, Any] = {
      Flow[Message]
        .map {
          // TODO: ? handle chunked messages
          case TextMessage.Strict(json) =>
            // incoming message from websocket
            decode[ClientEvent](json).map { msg =>
              playerActor ! ConnectionEvent(connectionId, msg)
            }

          case bm: BinaryMessage =>
            // ignore binary messages but drain content to avoid the stream being clogged
            bm.dataStream.runWith(Sink.ignore)
            Nil
        }
        .to(
          Sink.onComplete(_ => playerActor ! Disconnected(connectionId))
        )
    }

    playerActor ! Connected(connectionId, connectionRef)

    Flow.fromSinkAndSource(sink, Source.fromPublisher(publisher))
  }
}
