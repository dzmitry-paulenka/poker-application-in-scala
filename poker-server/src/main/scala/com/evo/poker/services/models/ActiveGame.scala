package com.evo.poker.services.models

import io.circe.generic.extras.ConfiguredJsonCodec
import com.evo.poker.services.http.Codecs._

@ConfiguredJsonCodec
case class ActiveGame(
  id: String,
  name: String,
  smallBlind: Int,
  buyIn: Int,
  playersCount: Int
)
