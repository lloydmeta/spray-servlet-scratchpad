package com.beachape.models

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import scala.annotation.meta.field
import org.apache.commons.validator.routines.UrlValidator

/**
 * For validating Urls
 */
object Url {
  val validSchemas = Seq("http", "https")
  val validator = new UrlValidator(validSchemas.toArray)
}

@ApiModel(description = "URL Scrape message")
case class UrlScrape(
                      @(ApiModelProperty @field)(value = "url description") url: String) {
  import Url.validator
  require(validator.isValid(url))
}