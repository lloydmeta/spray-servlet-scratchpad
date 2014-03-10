package com.beachape

import akka.actor.{Props, ActorSystem}
import spray.servlet.WebBoot
import com.beachape.controllers.ServiceActor

// this class is instantiated by the servlet initializer
// it needs to have a default constructor and implement
// the spray.servlet.WebBoot trait
class Boot extends WebBoot {

  // we need an ActorSystem to host our application in
  val system = ActorSystem("beachape")

  // the service actor replies to incoming HttpRequests
  val serviceActor = system.actorOf(Props[ServiceActor])

}