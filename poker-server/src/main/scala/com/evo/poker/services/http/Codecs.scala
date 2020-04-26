package com.evo.poker.services.http

import io.circe.{Codec, Decoder, Encoder}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{deriveConfiguredCodec, deriveEnumerationCodec}

import com.evo.poker.logic.{Card, GameTransition, Phase, Rank, Suit}
import com.evo.poker.services.actors.PlayerActor._

object Codecs {
  implicit val cardEncoder: Encoder[Card] =
    Encoder.forProduct2[Card, Char, Char]("rank", "suit")(card => (card.rank.symbol, card.suit.symbol))
  implicit val cardDecoder: Decoder[Card] =
    Decoder.forProduct2[Card, Char, Char]("rank", "suit")((r, s) => Card(Rank.fromChar(r).get, Suit.fromChar(s).get))

  implicit val phaseCodec: Codec[Phase] = EnumCodecs.phaseCodec

  implicit val gameTransitionCodec: Codec[GameTransition] = GameTransitionCodec.gtCodec

  implicit val serverEventCodec: Codec[ServerEvent] = EventCodecs.serverEventCodec
  implicit val clientEventCodec: Codec[ClientEvent] = EventCodecs.clientEventCodec
}

object EnumCodecs {
  implicit val circeConfig: Configuration = Configuration
    .default
    .withKebabCaseConstructorNames

  implicit val phaseCodec: Codec[Phase] = deriveEnumerationCodec[Phase]
}

object EventCodecs {
  implicit val circeConfig: Configuration = Configuration
    .default
    .withKebabCaseConstructorNames
    .withDiscriminator("eventType")

  implicit val gameTransitionCodec: Codec[GameTransition] = GameTransitionCodec.gtCodec

  implicit val serverEventCodec: Codec[ServerEvent] = deriveConfiguredCodec[ServerEvent]
  implicit val clientEventCodec: Codec[ClientEvent] = deriveConfiguredCodec[ClientEvent]
}

object GameTransitionCodec {
  implicit val gtConfig: Configuration = Configuration
    .default
    .withKebabCaseConstructorNames
    .withDiscriminator("transition")

  implicit val gtCodec: Codec[GameTransition] = deriveConfiguredCodec[GameTransition]
}
