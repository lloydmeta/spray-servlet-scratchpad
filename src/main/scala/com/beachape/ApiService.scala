package com.beachape

import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import models._
import UrlScrapeJsonSupport._
import spray.client.pipelining._

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiServiceActor extends Actor with ApiService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(apiRoutes)
}


// this trait defines our service behavior independently from the service actor
trait ApiService extends HttpService {

  implicit val exceutionContext = actorRefFactory.dispatcher

  val pipeline = sendReceive
  val apiRoutes =
    path("") {
      get {
        respondWithMediaType(`text/html`) { // XML is marshalled to `text/xml` by default, so we simply override here
          complete {
            <html>
              <body>
                <h1>Say hello to <i>spray-routing</i> on <i>Jetty</i>!</h1>
              </body>
            </html>
          }
        }
      }
    } ~
    path ("scrape_url"){
      post {
        respondWithMediaType(`application/json`) {
          entity(as[UrlScrape]) { urlScrape =>
            complete(
              pipeline(
                Get(
                  "http://metascraper.beachape.com/scrape/" + java.net.URLEncoder.encode(urlScrape.url, "UTF8")
                )
              ).map (_.entity.asString).recover { case _ => "failed" } )
          }
        }
      }
    }


}

