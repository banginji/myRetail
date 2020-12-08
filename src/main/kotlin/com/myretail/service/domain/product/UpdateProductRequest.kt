package com.myretail.service.domain.product

import com.myretail.service.domain.price.CurrentPrice

data class UpdateProductRequest(val current_price: CurrentPrice)