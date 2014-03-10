package com.beachape.controllers

import akka.actor.{ActorRefFactory, Actor}
import com.gettyimages.spray.swagger.SwaggerHttpService
import scala.reflect.runtime.universe._
import spray.util.LoggingContext
import spray.routing._
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes._
import com.beachape.controllers.api.ScrapeUrlService
import spray.routing.MalformedRequestContentRejection
import com.beachape.models.ScrapedData
import com.beachape.models.UrlScrape
import scala.Some
import com.beachape.models.ErrorResponse

/**
 * Central actor that handles routing.
 *
 * Spray recommends using this so that we can, if we want to, assign a standard
 * Akka router to Boot.scala that handles load-balancing
 *
 * We don't implement our route structure directly in the service actor because
 * we want to be able to test it independently, without having to spin up an actor
 */

class ServiceActor extends Actor with HttpService {

  // Absolutely necessary in order to support marshalling of error messages
  import JsonUnmarshallSupport._


  // The HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  // Declared "implicit" so we can pass it naturally into all other Service classes
  // that get instantiated further down
  implicit val actorRefFactory = context

  // Instantiate our Service classes
  val swaggerService: SwaggerHttpService = new SwaggerHttpService { // SwaggerHttpService is from swagger-spray by GettyImages
    override def actorRefFactory: ActorRefFactory = context
    override def apiTypes: List[Type] = List(typeOf[ScrapeUrlService])
    override def modelTypes: Seq[Type] = List(typeOf[UrlScrape], typeOf[ScrapedData], typeOf[ErrorResponse])
    override def apiVersion: String = "1.0"
    override def swaggerVersion: String = "1.2"
    override def baseUrl: String = "/api"
    override def specPath: String = "api-spec"
    override def resourcePath: String = "resources"
  }
  val apiScrapeUrlService = new ScrapeUrlService
  val swaggerUIService = new SwaggerUIService

  // Implicit Exception handler
  // See http://spray.io/documentation/1.1-SNAPSHOT/spray-routing/key-concepts/exception-handling/
  implicit def unsuccessfulResponseHandler(implicit log: LoggingContext) = ExceptionHandler {
    case e: UnsuccessfulResponseException => complete(
      UnprocessableEntity,
      ErrorResponse(
        s"The URL could not be processed. ${e.response.entity.asString}",
        e.getStackTraceString))
  }
  // Implicit Rejection handler
  // See http://spray.io/documentation/1.2.0/spray-routing/key-concepts/rejections/
  implicit def rejectionHandler = RejectionHandler {
    // Thrown by require in UrlScrape case class
    case MalformedRequestContentRejection(message, Some(throwable)) :: _ => complete(
      UnprocessableEntity,
      ErrorResponse(
        "The URL you requested may be invalid",
        throwable.getMessage
      )
    )
  }

  /**
   * Define the [[receive]] of this actor as the return of [[runRoute]],
   * passing in a route composed over routes obtained via extending various
   * [[spray.routing.HttpService]]s that actually define those routes
   */
  def receive = runRoute(
    apiScrapeUrlService.routes ~
    swaggerService.routes ~
    swaggerUIService.routes)
}