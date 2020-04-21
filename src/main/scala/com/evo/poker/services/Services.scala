package com.evo.poker.services

import com.evo.poker.services.actors.ActorService
import com.evo.poker.services.http.HttpService

// TODO: use some DI framework instead
object Services {
  lazy val actor = new ActorService()
  lazy val http  = new HttpService(actor)

  def init() = {
    http.init()
  }

  def stop() = {
    http.stop()
  }
}
