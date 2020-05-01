package com.evo.poker.services.http

import akka.http.scaladsl.model.{ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import com.evo.poker.PokerModules
import com.evo.poker.services.http.dto.UserResponse

class EndpointUsersTest extends AnyFlatSpec with Matchers with EitherValues with ScalatestRouteTest {

  val modules = new PokerModules
  val routes  = modules.routes
  val encoder = modules.encodingService

  val random = new scala.util.Random()

  it should "handle user api requests" in {
    val randomName  = random.alphanumeric.take(10).mkString("")
    val userRequest = s""" { "username": "${randomName}", "password": "asdf" } """

    Post(s"/api/users/signup", json(userRequest)) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      entityAs[UserResponse].username shouldBe randomName
    }

    Post(s"/api/users/login", json(userRequest)) ~> routes ~> check {
      status shouldBe StatusCodes.OK
      entityAs[UserResponse].username shouldBe randomName
    }
  }

  it should "handle error request properly" in {
    val randomName  = random.alphanumeric.take(10).mkString("")
    val userRequest = s""" { "username": "${randomName}", "password": "asdf" } """

    Post(s"/api/users/login", json(userRequest)) ~> routes ~> check {
      status shouldBe StatusCodes.Unauthorized
    }

    Post(s"/api/users/signup", json(userRequest)) ~> routes ~> check {
      status shouldBe StatusCodes.OK
    }

    Post(s"/api/users/signup", json(userRequest)) ~> routes ~> check {
      status shouldBe StatusCodes.Conflict
    }

    Post(s"/api/users/login", json(userRequest)) ~> routes ~> check {
      status shouldBe StatusCodes.OK
    }

    val invalidPass = s""" { "username": "${randomName}", "password": "asdfasdf" } """
    Post(s"/api/users/login", json(invalidPass)) ~> routes ~> check {
      status shouldBe StatusCodes.Unauthorized
    }
  }

  def json(s: String) = {
    HttpEntity(ContentTypes.`application/json`, s)
  }
}
