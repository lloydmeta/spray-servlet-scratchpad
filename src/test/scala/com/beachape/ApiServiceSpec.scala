package com.beachape

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._
import models._
import models.UrlScrapeJsonSupport._
import scala.concurrent.duration._

class ApiServiceSpec extends Specification with Specs2RouteTest with ApiService {

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(5, SECONDS))

  def actorRefFactory = system
  
  "ApiService" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> apiRoutes ~> check {
        responseAs[String] must contain("Say hello")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> apiRoutes ~> check {
        handled must beFalse
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Put() ~> sealRoute(apiRoutes) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }

    "return an OK response for POST requests to scrape_url" in {
      Post("/scrape_url", UrlScrape("http://www.google.com")) ~> apiRoutes ~> check {
        status === OK
      }
    }
  }
}
