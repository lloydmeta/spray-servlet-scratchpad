package com.beachape.resources.api

import spray.routing.{HttpService, Route}

/**
 * Trait for ApiService classes to inherit from
 */
trait ResourceBase extends HttpService {

  val pathPrefix = "api"
  def declaredRoutes: Seq[Route]

  def routes: Route = pathPrefix(pathPrefix) {
    declaredRoutes.reduceLeft(_ ~ _)
  }
}
