package com.evo.poker.services.http

import akka.http.scaladsl.model.StatusCodes.{Conflict, Unauthorized}
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import org.bson.types.ObjectId

import scala.concurrent.ExecutionContext

import com.evo.poker.services.db.{UserEntity, UserRepository}
import com.evo.poker.services.http.Codecs._
import com.evo.poker.services.http.dto._

class EndpointUsers(repository: UserRepository, encoder: EncodingService)(implicit ec: ExecutionContext, mat: Materializer) {
  val routes = cors() {
    pathPrefix("api" / "users") {
      (post & path("signup") & entity(as[SingupRequest])) { req =>
        onSuccess(repository.findByUsername(req.username)) {
          case Some(_) =>
            complete(HttpResponse(Conflict))
          case None =>
            val passwordHash = encoder.hashPassword(req.password)
            val user         = UserEntity(ObjectId.get(), req.username, passwordHash, 4200)
            onSuccess(repository.create(user)) { userId =>
              val token = encoder.encodeJwt(userId)
              complete(UserResponse(req.username, token))
            }
        }
      } ~ (post & path("login") & entity(as[LoginRequest])) { req =>
        onSuccess(repository.findByUsername(req.username)) {
          case Some(user) =>
            if (!encoder.checkPassword(req.password, user.passwordHash)) {
              complete(HttpResponse(Unauthorized))
            } else {
              val token = encoder.encodeJwt(user._id.toHexString)
              complete(UserResponse(req.username, token))
            }
          case None =>
            complete(HttpResponse(Unauthorized))
        }
      } ~ (post & path("check-token") & entity(as[CheckTokenRequest])) { req =>
        encoder.decodeJwt(req.authToken) match {
          case Left(_) =>
            complete(CheckTokenResponse(false))
          case Right(userId) =>
            onSuccess(repository.findById(userId)) {
              case Some(user) =>
                complete(CheckTokenResponse(true))
              case None =>
                complete(CheckTokenResponse(false))
            }
        }
      }
    }
  }
}
