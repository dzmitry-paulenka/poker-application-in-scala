package com.evo.poker.services.http

import io.circe.Codec
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.deriveConfiguredCodec

import com.evo.poker.logic.{Card, GameTransition, Phase, Rank, Suit}
import com.evo.poker.services.actors.PlayerActor._

object Codecs {
  implicit val circeConfig: Configuration = Configuration
    .default
    .withKebabCaseConstructorNames
    .withDiscriminator("eventType")

  implicit val gameTransitionCodec: Codec[GameTransition] = deriveConfiguredCodec[GameTransition]
  implicit val phaseCodec: Codec[Phase] = deriveConfiguredCodec[Phase]

  implicit val suitCodec: Codec[Suit] = deriveConfiguredCodec[Suit]
  implicit val rankCodec: Codec[Rank] = deriveConfiguredCodec[Rank]
  implicit val cardCodec: Codec[Card] = deriveConfiguredCodec[Card]

  implicit val serverMsgCodec: Codec[ServerEvent]         = deriveConfiguredCodec[ServerEvent]
  implicit val clientMsgCodec: Codec[ClientEvent]         = deriveConfiguredCodec[ClientEvent]
}
