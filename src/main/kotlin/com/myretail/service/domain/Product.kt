package com.myretail.service.domain

data class ProductResponse(val id: Int, val name: String, val current_price: CurrentPrice?)

data class CurrentPrice(val value: Double, val currency_code: String)