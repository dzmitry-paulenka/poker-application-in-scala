package com.evo.poker

object PokerServerApp extends App {
  val modules = new PokerModules

  import modules._

  httpService.start()

  sys.addShutdownHook {
    httpService.stop()
    actorService.stop()
  }
}
