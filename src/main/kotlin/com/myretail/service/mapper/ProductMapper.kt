package com.myretail.service.mapper

import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.domain.price.UpdateProductPriceRequest
import java.util.function.Function

fun updateDataMapper() = Function<UpdateProductRequest, UpdateProductPriceRequest> { (current_price) -> UpdateProductPriceRequest(current_price) }