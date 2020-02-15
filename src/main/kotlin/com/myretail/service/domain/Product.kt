package com.myretail.service.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.myretail.service.persistence.ProductPrice

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResponse(val id: Int?, val name: String?, val current_price: CurrentPrice?, val productError: List<Error?> = emptyList())

data class CurrentPrice(val value: Double?, val currency_code: String?)

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductPriceResponse(val productPrice: ProductPrice?, val productPriceError: ProductPriceError? = null)