package com.myretail.service.domain.price

import com.fasterxml.jackson.annotation.JsonInclude
import com.myretail.service.domain.ProductError

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductPriceResponse(val productPrice: ProductPrice?, val productPriceError: ProductError? = null)