package com.evo.poker.services.http
import io.circe.generic.JsonCodec

package object dto {

  @JsonCodec
  final case class SingupRequest(username: String, password: String)

  @JsonCodec
  final case class LoginRequest(username: String, password: String)

  @JsonCodec
  final case class CheckTokenRequest(username: String, authToken: String)

  @JsonCodec
  final case class CheckTokenResponse(valid: Boolean)

  @JsonCodec
  final case class UserResponse(username: String, authToken: String)

  @JsonCodec
  final case class ErrorResponse(error: String)
}
