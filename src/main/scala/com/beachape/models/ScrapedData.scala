package com.beachape.models

import com.wordnik.swagger.annotations.{ApiModelProperty, ApiModel}
import scala.annotation.meta.field

@ApiModel(description = "URL Scraped data")
case class ScrapedData (
                         @(ApiModelProperty @field)(value = "Url that this was scraped from") url: String,
                         @(ApiModelProperty @field)(value = "Title") title: String,
                         @(ApiModelProperty @field)(value = "Main Image URL") mainImageUrl: String,
                         @(ApiModelProperty @field)(value = "Description") description: String,
                         @(ApiModelProperty @field)(value = "Image urls") imageUrls: Seq[String]
                         )