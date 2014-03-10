package com.beachape.controllers

import spray.routing._
import spray.http._
import MediaTypes._
import spray.client.pipelining._
import com.beachape.models._
import com.wordnik.swagger.annotations._
import scala.concurrent.Future
import spray.util.LoggingContext
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes._

/**
 * This trait defines the api/scrape_url path of our API and it does so
 * independently from the service actor. This is the rough equivalent of a "service"
 * in DropWizard.
 *
 * Note the at the value of the @Api annotation is set to /scrape_url
 * This is because baseUrl is set to /api in our instantiation of
 * [[com.gettyimages.spray.swagger.SwaggerHttpService]] in [[ServiceActor]].
 * This appears to be necessary because otherwise SwaggerUI will make requests
 * relative to the specPath ('api-spec')..might be fixable
  */
@Api(value = "/scrape_url", description = "Allows you to scrape a URL using an external call to http://metascraper.beachape.com")
trait ApiScrapeUrlService extends HttpService {

  // Absolutely necessary in order to support marshalling.
  import JsonUnmarshallSupport._

  implicit val executionContext = actorRefFactory.dispatcher

  val pipeline = sendReceive ~> unmarshal[ScrapedData]
  val pathPrefix = "api"

  // All the routes in this trait composed.
  val apiScrapeUrlRoutes = pathPrefix(pathPrefix) {
    scrapeRoute
  }

  @ApiOperation(
    value = "Scrape a URL ",
    notes = " Sends a request to http://metascraper.beachape.com to get it scraped.",
    httpMethod = "POST",
    response = classOf[ScrapedData]
  )
  @ApiImplicitParams(Array(
    new ApiImplicitParam(
      name = "url",
      value = "The URL to scrape. This should be a JSON string, e.g. { \"url\" : \"http://www.beachape.com/\" } ",
      required = true,
      dataType = "UrlScrape",
      paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Server error "),
    new ApiResponse(code = 422, message = "Unprocessable URL", response = classOf[ErrorResponse])
  ))
  def scrapeRoute = path ("scrape_url"){
    post {
      respondWithMediaType(`application/json`) {
        entity(as[UrlScrape]) { urlScrape =>
          complete (scrapeFuture(urlScrape.url))
        }
      }
    }
  }

  /**
   * Given a URL, asynchronously hits http://metascraper.beachape.com
   * to get metadata
   *
   * @param url
   * @return
   */
  def scrapeFuture(url: String): Future[ScrapedData] = pipeline(Get(
      "http://metascraper.beachape.com/scrape/" + java.net.URLEncoder.encode(url, "UTF8")
    )
  )

}