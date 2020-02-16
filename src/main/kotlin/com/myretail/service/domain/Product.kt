package com.myretail.service.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.myretail.service.persistence.ProductPrice

data class ProductRequest(val current_price: CurrentPrice)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResponse(val id: Int?, val name: String?, val current_price: CurrentPrice?, val productErrors: List<WrapperError?> = emptyList())

data class CurrentPrice(val value: Double?, val currency_code: String?)

data class ProductPriceRequest(val current_price: CurrentPrice)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductPriceResponse(val productPrice: ProductPrice?, val productPriceError: ProductPriceError? = null)