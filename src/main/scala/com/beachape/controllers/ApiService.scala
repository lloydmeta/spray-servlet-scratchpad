package com.beachape.controllers

import akka.actor.{ActorRefFactory, Actor}
import spray.routing._
import spray.http._
import MediaTypes._
import spray.client.pipelining._
import scala.reflect.runtime.universe._
import com.beachape.models._
import com.wordnik.swagger.annotations._
import com.gettyimages.spray.swagger.SwaggerHttpService
import scala.concurrent.Future
import spray.util.LoggingContext
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiServiceActor extends Actor with ApiService {

  val swaggerService: SwaggerHttpService = new SwaggerHttpService {
    override def actorRefFactory: ActorRefFactory = context
    override def apiTypes: List[Type] = List(typeOf[ApiService])
    override def modelTypes: Seq[Type] = List(typeOf[UrlScrape], typeOf[ScrapedData], typeOf[ErrorResponse])
    override def apiVersion: String = "1.0"
    override def swaggerVersion: String = "1.2"
    override def baseUrl: String = "/api"
    override def specPath: String = "api-spec"
    override def resourcePath: String = "resources"
  }

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(apiRoutes ~ swaggerService.routes ~ swaggerUI )
}


// this trait defines our service behavior independently from the service actor
@Api(value = "/scrape_url", description = "Allows you to scrape a URL using an external call to http://metascraper.beachape.com")
trait ApiService extends HttpService {

  import JsonUnmarshallSupport._

  implicit val exceutionContext = actorRefFactory.dispatcher
  implicit def unsuccessfulResponseHandler(implicit log: LoggingContext) = ExceptionHandler {
    case e: UnsuccessfulResponseException => complete(
      UnprocessableEntity,
      ErrorResponse(
        s"The URL could not be processed. ${e.response.entity.asString}",
        e.getStackTraceString))
  }

  val pipeline = sendReceive ~> unmarshal[ScrapedData]
  val apiRoutes = scrapeRoute

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
  def scrapeRoute = path ("api" / "scrape_url"){
    post {
      respondWithMediaType(`application/json`) {
        entity(as[UrlScrape]) { urlScrape =>
          complete (scrapeFuture(urlScrape.url))
        }
      }
    }
  }

  val swaggerUI = path("swagger") {
    pathEnd { redirect("/swagger/", StatusCodes.PermanentRedirect) } } ~
    pathPrefix("swagger") {
      pathSingleSlash { getFromResource("swagger/index.html") } ~
      getFromResourceDirectory("swagger")
    }

  def scrapeFuture(url: String): Future[ScrapedData] = pipeline( Get(
      "http://metascraper.beachape.com/scrape/" + java.net.URLEncoder.encode(url, "UTF8")
    )
  )

}