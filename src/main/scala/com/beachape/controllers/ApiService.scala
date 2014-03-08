package com.beachape.controllers

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import spray.client.pipelining._
import com.beachape.models.UrlScrape
import scala.reflect.runtime.universe._
import com.beachape.models.UrlScrapeJsonSupport._
import com.wordnik.swagger.annotations._
import com.gettyimages.spray.swagger.SwaggerHttpService

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiServiceActor extends Actor with ApiService with SwaggerHttpService {

  val baseUrl = "swaggur"
  val specPath = "swagger"
  val resourcePath = "swagger_resources"
  val apiTypes = Seq(typeOf[ApiService])
  val swaggerVersion = "1.2"
  val apiVersion = "0.0.1"
  val modelTypes = Seq(typeOf[UrlScrape])


  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(apiRoutes ~ routes)
}


// this trait defines our service behavior independently from the service actor
@Api(value = "/scrape_url", description = "Allows you to scrape a URL using an external call to http://metascraper.beachape.com")
trait ApiService extends HttpService {

  implicit val exceutionContext = actorRefFactory.dispatcher

  val pipeline = sendReceive
  val apiRoutes = scrapeRoute

  @ApiOperation(value = "Scrape a URL.", notes = "Sends a request to http://metascraper.beachape.com to get it scraped", response = classOf[UrlScrape], httpMethod = "POST")
  @ApiImplicitParams(Array(
    new ApiImplicitParam(name = "url", value = "The URL to scrape", required = true, dataType = "String", paramType = "body")
  ))
  @ApiResponses(Array(
    new ApiResponse(code = 500, message = "Server error ")
  ))
  def scrapeRoute = path ("scrape_url"){
    post {
      respondWithMediaType(`application/json`) {
        entity(as[UrlScrape]) { urlScrape =>
          complete(
            pipeline(
              Get(
                "http://metascraper.beachape.com/scrape/" + java.net.URLEncoder.encode(urlScrape.url, "UTF8")
              )
            ).map (_.entity.asString) )
        }
      }
    }
  }

}

