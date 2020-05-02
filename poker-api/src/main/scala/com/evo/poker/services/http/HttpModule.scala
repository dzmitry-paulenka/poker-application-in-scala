package com.evo.poker.services.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.softwaremill.macwire.{Module, wire}

import com.evo.poker.services.actors.ActorsModule
import com.evo.poker.services.config.ConfigModule
import com.evo.poker.services.db.DbModule

@Module
trait HttpModule extends ConfigModule with ActorsModule with DbModule {
  private val usersEndpoint: EndpointUsers    = wire[EndpointUsers]
  private val eventsEndpoint: EndpointEvents  = wire[EndpointEvents]
  private val hcEndpoint: EndpointHealthCheck = wire[EndpointHealthCheck]

  lazy val httpService = wire[HttpService]

  lazy val encodingService = wire[EncodingService]

  lazy val routes = Route.seal {
    usersEndpoint.routes ~ eventsEndpoint.routes ~ hcEndpoint.routes
  }
}
