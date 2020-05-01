package com.evo.poker.services.http

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class EndpointHealthCheck {
  val routes: Route = {
    pathPrefix("api" / "health-check") {
      get { complete("ok") }
    }
  }
}
