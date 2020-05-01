package com.evo.poker.services.http
import io.circe.generic.JsonCodec

package object dto {

  @JsonCodec
  case class SingupRequest(username: String, password: String)

  @JsonCodec
  case class LoginRequest(username: String, password: String)

  @JsonCodec
  case class CheckTokenRequest(username: String, authToken: String)

  @JsonCodec
  case class CheckTokenResponse(valid: Boolean)

  @JsonCodec
  case class UserResponse(username: String, authToken: String)

  @JsonCodec
  case class ErrorResponse(error: String)
}
