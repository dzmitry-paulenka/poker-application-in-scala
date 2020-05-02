package com.evo.poker.services.http

import java.time.{Instant, Period}

import com.typesafe.config.Config
import org.mindrot.jbcrypt.BCrypt
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import com.evo.poker.services.http.Codecs._

class EncodingService(config: Config) {
  private val secretKey = config.getString("jwt.key")
  private val algorithm = JwtAlgorithm.HS256

  def encodeJwt(subject: String): String = {
    val claim = JwtClaim(
      subject = Some(subject),
      expiration = Some(Instant.now.plus(Period.ofDays(30)).getEpochSecond),
      issuedAt = Some(Instant.now.getEpochSecond)
    )
    JwtCirce.encode(claim, secretKey, algorithm)
  }

  def decodeJwt(token: String): Either[String, String] = {
    JwtCirce
      .decode(token, secretKey, Seq(algorithm))
      .toEither
      .swap
      .map(_.getMessage)
      .swap
      .flatMap(_.subject.toRight("Subject isn't found"))
  }

  def hashPassword(password: String): String = {
    BCrypt.hashpw(password, BCrypt.gensalt());
  }

  def checkPassword(password: String, passwordHash: String): Boolean = {
    BCrypt.checkpw(password, passwordHash)
  }
}
