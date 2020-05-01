package com.evo.poker.services.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import com.evo.poker.PokerModules

class EndpointHealthCheckTest extends AnyFlatSpec with Matchers with ScalatestRouteTest {

  val modules = new PokerModules
  val routes  = modules.routes

  it should "handle a health check request" in {
    Get(s"/api/health-check") ~> routes ~> check {
      status shouldBe StatusCodes.OK
      responseAs[String] shouldBe "ok"
    }
  }
}
