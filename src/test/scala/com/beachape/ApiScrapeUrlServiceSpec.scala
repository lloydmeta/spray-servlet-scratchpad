package com.beachape

import org.scalatest.{ShouldMatchers, FunSpec}
import spray.testkit.ScalatestRouteTest
import spray.http._
import StatusCodes._
import models._
import controllers.JsonUnmarshallSupport._
import scala.concurrent.duration._
import com.beachape.controllers.ApiScrapeUrlService
import java.lang.IllegalArgumentException

class ApiScrapeUrlServiceSpec extends FunSpec with ScalatestRouteTest with ApiScrapeUrlService with ShouldMatchers {

  implicit val routeTestTimeout = RouteTestTimeout(FiniteDuration(5, SECONDS))

  def actorRefFactory = system

  describe("ApiService") {
    it ("leave GET requests to other paths unhandled") {
      Get("/kermit") ~> apiScrapeUrlRoutes ~> check {
        handled should be (false)
      }
    }

    it("return a MethodNotAllowed error for PUT requests to the scrape path") {
      Put("/api/scrape_url") ~> sealRoute(apiScrapeUrlRoutes) ~> check {
        status should be (MethodNotAllowed)
        responseAs[String] should be ("HTTP method not allowed, supported methods: POST")
      }
    }

    it("should return an OK response for POST requests to scrape_url") {
      Post("/api/scrape_url", UrlScrape("http://www.google.com")) ~> apiScrapeUrlRoutes ~> check {
        status should be (OK)
      }
    }

    it("should raise an IllegalArgument for POST requests to scrape_url with invalid URL") {
      intercept[IllegalArgumentException] {
        Post("/api/scrape_url", UrlScrape("hasdfattp://www.google.com")) ~> apiScrapeUrlRoutes ~> check {
          println("This will not happen")
        }
      }
    }

  }
}
