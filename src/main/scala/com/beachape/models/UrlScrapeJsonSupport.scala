package com.beachape.models

import spray.httpx.SprayJsonSupport
import spray.json.DefaultJsonProtocol
import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}


@ApiModel(description = "URL Scrape message")
case class UrlScrape(
                      @ApiModelProperty(value = "TODO: url description") url: String)

/**
 * Acts as an in-scope unmarshaller of JSON content if we
 * import this into the scope of a route
 */
object UrlScrapeJsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val UrlScrapeFormats = jsonFormat1(UrlScrape)
}