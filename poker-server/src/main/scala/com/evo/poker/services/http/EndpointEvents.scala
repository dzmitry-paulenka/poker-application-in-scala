package com.evo.poker.services.http

import java.util.UUID

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes.Unauthorized
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.http.scaladsl.server.Directives._
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import io.circe.parser.decode
import io.circe.syntax._
import org.reactivestreams.Publisher

import com.evo.poker.services.actors.ActorService
import com.evo.poker.services.actors.PlayerActor._
import com.evo.poker.services.db.{UserEntity, UserRepository}
import com.evo.poker.services.http.Codecs._

class EndpointEvents(actorService: ActorService, repository: UserRepository, encoder: EncodingService)(implicit val materializer: Materializer) {

  // this way of passing a token is not secure, but it's too hard to do it properly for websockets
  val routes = pathPrefix("api" / "events") {
    parameter("authToken") { token =>
      encoder.decodeJwt(token) match {
        case Left(_) =>
          complete(Unauthorized)
        case Right(userId) =>
          onSuccess(repository.findById(userId)) {
            case Some(user) =>
              handleWebSocketMessages(
                handlePlayerConnection(user)
              )
            case None =>
              complete(Unauthorized)
          }
      }
    }
  }

  private def handlePlayerConnection(user: UserEntity): Flow[Message, Message, Any] = {
    val playerActor: ActorRef = actorService.playerActor(user)
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
