package com.beachape.models

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import scala.annotation.meta.field

@ApiModel(description = "URL Scrape message")
case class UrlScrape(
                      @(ApiModelProperty @field)(value = "url description") url: String)