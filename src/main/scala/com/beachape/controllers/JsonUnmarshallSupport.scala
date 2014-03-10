package com.beachape.controllers

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import com.beachape.models.{ErrorResponse, ScrapedData, UrlScrape}

/**
 * Define our JSON <--> Model formatting here so we can support (de)marshalling
 * easily in our "controllers"
 */
object JsonUnmarshallSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val urlScrapeFormat = jsonFormat1(UrlScrape)
  implicit val scrapedDataFormat = jsonFormat5(ScrapedData)
  implicit val errorDataFormat = jsonFormat2(ErrorResponse)
}
