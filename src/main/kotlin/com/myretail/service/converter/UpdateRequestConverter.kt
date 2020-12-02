package com.myretail.service.converter

import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.domain.price.UpdateProductPriceRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UpdateRequestConverter : Converter<UpdateProductRequest, UpdateProductPriceRequest> {
    override fun convert(source: UpdateProductRequest): UpdateProductPriceRequest = UpdateProductPriceRequest(source.current_price)
}