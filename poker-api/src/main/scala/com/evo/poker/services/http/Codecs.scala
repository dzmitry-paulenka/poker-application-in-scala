package com.evo.poker.services.http

import io.circe._
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._

import com.evo.poker.logic._
import com.evo.poker.services.actors.PlayerActor._

object Codecs {
  implicit val cardEncoder: Encoder[Card] =
    Encoder.forProduct2[Card, Char, Char]("rank", "suit")(card => (card.rank.symbol, card.suit.symbol))

  implicit val cardDecoder: Decoder[Card] = new Decoder[Card] {
    final def apply(c: HCursor): Decoder.Result[Card] =
      for {
        rChar <- c.downField("rank").as[Char]
        rank  <- Rank.fromChar(rChar).toRight(DecodingFailure(s"invalid rank $rChar", c.history))
        sChar <- c.downField("suit").as[Char]
        suit  <- Suit.fromChar(sChar).toRight(DecodingFailure(s"invalid suit $sChar", c.history))
      } yield {
        new Card(rank, suit)
      }
  }

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
