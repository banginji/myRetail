package com.myretail.service.domain.price

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductPriceResponse(val productPrice: ProductPrice?, val productPriceError: ProductPriceError? = null)