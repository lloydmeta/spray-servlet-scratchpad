package com.beachape.resources.api

import spray.http._
import MediaTypes._
import spray.client.pipelining._
import com.beachape.models._
import com.wordnik.swagger.annotations._
import scala.concurrent.Future
import com.beachape.resources.JsonUnmarshallSupport
import akka.actor.ActorRefFactory

/**
 * Defines the /api/scrape_url path of our API. This is the rough equivalent of a "service"
 * in DropWizard.
 *
 * Note that the value of the @Api annotation is set to /scrape_url. This is because
 * this class extends ApiService, which defines the `routes` method to be a reduceLeft
 * over the `declaredRoutes` sequence, and then puts it under the '/api' prefix
 *
 * This appears to be necessary because otherwise SwaggerUI will make requests
 * relative to the specPath ('api-spec').. need to check if this is proper behaviour
  */
@Api(value = "/scrape_url", description = "Allows you to scrape a URL using an external call to http://metascraper.beachape.com")
class ScrapeUrl(implicit val actorRefFactory: ActorRefFactory) extends ResourceBase {

  // Absolutely necessary in order to support marshalling.
  import JsonUnmarshallSupport._
  implicit val executionContext = actorRefFactory.dispatcher

  val pipeline = sendReceive ~> unmarshal[ScrapedData]

  // All the routes in this class composed.
  val declaredRoutes = Seq(scrapeRoute)

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