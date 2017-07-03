package com.shoppingbasket

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import com.shoppingbasket.api.ShoppingAPI
import com.shoppingbasket.util.ConfigParams

import scala.concurrent.Future

object Main extends App {

  implicit val system = ActorSystem()
  implicit val ec = system.dispatcher

  val api = new ShoppingAPI(system).routes

  implicit val materializer = ActorMaterializer()
  val bindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(api, ConfigParams.host, ConfigParams.port)

  val log = Logging(system.eventStream, "shopping-basket")
  bindingFuture.map { serverBinding =>
    log.info(s"ShoppingBasketAPI bound to ${serverBinding.localAddress} ")
  }.failed.foreach {
    case ex: Exception =>
      log.error(ex, "Failed to bind to {}:{}!", ConfigParams.host, ConfigParams.port)
      system.terminate()
  }
}
