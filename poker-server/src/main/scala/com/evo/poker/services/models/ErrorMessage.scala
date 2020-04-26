package com.evo.poker.services.models

import io.circe.generic.extras.ConfiguredJsonCodec
import com.evo.poker.services.http.Codecs._

@ConfiguredJsonCodec
case class ErrorMessage(error: String)
