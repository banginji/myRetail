package com.myretail.service.domain

import com.myretail.service.domain.price.CurrentPrice

data class UpdateProductRequest(val current_price: CurrentPrice)