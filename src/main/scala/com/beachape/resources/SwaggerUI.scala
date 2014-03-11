package com.beachape.resources

import spray.http.StatusCodes
import spray.routing.HttpService
import akka.actor.ActorRefFactory

/**
 * Service that implements the route for SwaggerUI
 */
class SwaggerUI(implicit val actorRefFactory: ActorRefFactory) extends HttpService {

  def routes = path("swagger") {
    pathEnd { redirect("/swagger/", StatusCodes.PermanentRedirect) } } ~
    pathPrefix("swagger") {
      pathSingleSlash { getFromResource("swagger/index.html") } ~
        getFromResourceDirectory("swagger")
    }

}
