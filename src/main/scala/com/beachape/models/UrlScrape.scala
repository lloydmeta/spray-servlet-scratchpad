package com.beachape.models

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import scala.annotation.meta.field

@ApiModel(description = "URL Scrape message")
case class UrlScrape(
                      @(ApiModelProperty @field)(value = "TODO: url description") url: String)