package com.evo.poker.services.config

import com.softwaremill.macwire.Module
import com.typesafe.config.ConfigFactory

@Module
trait ConfigModule {
  lazy val config = ConfigFactory.load()
}
