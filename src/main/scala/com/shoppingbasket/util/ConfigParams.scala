package com.shoppingbasket.util

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._

import scala.concurrent.duration.FiniteDuration

object ConfigParams {
  private val config = ConfigFactory.load()

  val host = config.getString("http.host")
  val port = config.getInt("http.port")

  private val duration = config.as[FiniteDuration]("akka.http.server.request-timeout")
  val timeout = FiniteDuration(duration.length, duration.unit)
}
