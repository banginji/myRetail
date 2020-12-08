package com.myretail.service.domain.price

import com.myretail.service.domain.product.ProductError

data class PriceResponse(val price: Price?, val productPriceError: ProductError? = null)