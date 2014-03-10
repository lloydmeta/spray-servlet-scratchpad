package com.beachape.controllers.api

import akka.actor.ActorRefFactory
import com.gettyimages.spray.swagger.SwaggerHttpService
import scala.reflect.runtime.universe._
import com.beachape.models._

/**
 * Service that implements SwaggerHttpService, which is from swagger-spray by GettyImages
 */
class SwaggerResourcesService (implicit val actorRefFactory: ActorRefFactory) extends SwaggerHttpService {
  def apiTypes = Seq(typeOf[ScrapeUrlService])
  def modelTypes = Seq(typeOf[UrlScrape], typeOf[ScrapedData], typeOf[ErrorResponse])
  def apiVersion = "1.0"
  def swaggerVersion = "1.2"
  def baseUrl = "/api"
  def specPath = "api-spec"
  def resourcePath = "resources"
}