package com.evo.poker

import com.evo.poker.services.Services

object PokerServerApp extends App {
  Services.init()

  sys.addShutdownHook {
    Services.stop()
  }
}
