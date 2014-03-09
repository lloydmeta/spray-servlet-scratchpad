package com.beachape

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import models._
import controllers.JsonUnmarshallSupport._
import scala.concurrent.duration._
import com.beachape.controllers.ApiService

class ApiServiceSpec extends Specification with Specs2RouteTest with ApiService {

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(5, SECONDS))

  def actorRefFactory = system

  "ApiService" should {

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> apiRoutes ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the scrape path" in {
      Put("/api/scrape_url") ~> sealRoute(apiRoutes) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: POST"
      }
    }

    "return an OK response for POST requests to scrape_url" in {
      Post("/api/scrape_url", UrlScrape("http://www.google.com")) ~> apiRoutes ~> check {
        status === OK
      }
    }
  }
}
