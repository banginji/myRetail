package com.myretail.service.domain

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RedSkyResponse(val product: RedSkyProduct?, val redSkyError: RedSkyError?)

data class RedSkyProduct(val item: RedSkyProductItem)

data class RedSkyProductItem(val tcin: String, val product_description: RedSkyProductItemDesc)

data class RedSkyProductItemDesc(val title: String)
