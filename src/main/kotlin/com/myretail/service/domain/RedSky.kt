package com.myretail.service.domain

import com.fasterxml.jackson.annotation.JsonInclude

data class RedSkyResponse(val product: RedSkyProduct)

data class RedSkyProduct(val item: RedSkyProductItem)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RedSkyProductItem(val product_description: RedSkyProductItemDesc?)

data class RedSkyProductItemDesc(val title: String)