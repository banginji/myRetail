package com.myretail.service.domain

data class RedSky(val product: RedSkyProduct)

data class RedSkyProduct(val item: RedSkyProductItem)

data class RedSkyProductItem(val product_description: RedSkyProductItemDesc?)

data class RedSkyProductItemDesc(val title: String)