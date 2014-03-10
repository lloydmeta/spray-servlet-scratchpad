package com.beachape.controllers

import spray.http.StatusCodes
import spray.routing.HttpService

/**
 * Service that implements the route for SwaggerUI
 */
trait SwaggerUIService extends HttpService {

  val swaggerUI = path("swagger") {
    pathEnd { redirect("/swagger/", StatusCodes.PermanentRedirect) } } ~
    pathPrefix("swagger") {
      pathSingleSlash { getFromResource("swagger/index.html") } ~
        getFromResourceDirectory("swagger")
    }

}
