package com.beachape.controllers.api

import org.scalatest.{ShouldMatchers, FunSpec}
import spray.testkit.ScalatestRouteTest
import spray.http._
import StatusCodes._
import scala.concurrent.duration._
import java.lang.IllegalArgumentException
import spray.routing.HttpService
import com.beachape.models.UrlScrape
import com.beachape.controllers.JsonUnmarshallSupport

class ScrapeUrlServiceSpec extends FunSpec with ScalatestRouteTest with ShouldMatchers with HttpService {

  import JsonUnmarshallSupport._

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(5, SECONDS))
  implicit val actorRefFactory = system
  val scrapeUrlService = new ScrapeUrlService
  val routes = scrapeUrlService.routes

  describe("ApiService") {
    it ("leave GET requests to other paths unhandled") {
      Get("/kermit") ~> routes ~> check {
        handled should be (false)
      }
    }

    it("return a MethodNotAllowed error for PUT requests to the scrape path") {
      Put("/api/scrape_url") ~> sealRoute(routes) ~> check {
        status should be (MethodNotAllowed)
        responseAs[String] should be ("HTTP method not allowed, supported methods: POST")
      }
    }

    it("should return an OK response for POST requests to scrape_url") {
      Post("/api/scrape_url", UrlScrape("http://www.google.com")) ~> routes ~> check {
        status should be (OK)
      }
    }

    it("should raise an IllegalArgument for POST requests to scrape_url with invalid URL") {
      intercept[IllegalArgumentException] {
        Post("/api/scrape_url", UrlScrape("hasdfattp://www.google.com")) ~> routes ~> check {
          println("This will not happen")
        }
      }
    }

  }
}
