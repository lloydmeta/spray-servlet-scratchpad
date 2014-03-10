package com.beachape.controllers

import akka.actor.{ActorRefFactory, Actor}
import com.gettyimages.spray.swagger.SwaggerHttpService
import com.beachape.models.{ErrorResponse, ScrapedData, UrlScrape}
import scala.reflect.runtime.universe._
import spray.util.LoggingContext
import spray.routing.{MalformedRequestContentRejection, ValidationRejection, RejectionHandler, ExceptionHandler}
import spray.httpx.UnsuccessfulResponseException
import spray.http.StatusCodes._
import com.beachape.models.ScrapedData
import com.beachape.models.UrlScrape
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

class ServiceActor extends Actor with ApiScrapeUrlService with SwaggerUIService {

  // Absolutely necessary in order to support marshalling of error messages
  import JsonUnmarshallSupport._

  /**
   * SwaggerHttpService is from swagger-spray by GettyImages
   */
  val swaggerService: SwaggerHttpService = new SwaggerHttpService {
    override def actorRefFactory: ActorRefFactory = context
    override def apiTypes: List[Type] = List(typeOf[ApiScrapeUrlService])
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
    apiScrapeUrlRoutes ~
    swaggerService.routes ~
    swaggerUI)
}