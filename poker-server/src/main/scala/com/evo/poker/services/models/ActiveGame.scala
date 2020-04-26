package com.evo.poker.services.models

import io.circe.generic.JsonCodec

import com.evo.poker.services.http.Codecs._

@JsonCodec
case class ActiveGame(
  id: String,
  name: String,
  smallBlind: Int,
  buyIn: Int,
  playerCount: Int
)
