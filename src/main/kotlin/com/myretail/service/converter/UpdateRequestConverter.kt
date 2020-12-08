package com.myretail.service.converter

import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.product.UpdateProductRequest
import com.myretail.service.domain.price.UpdatePriceRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UpdateRequestConverter : Converter<UpdateProductRequest, UpdatePriceRequest> {
    override fun convert(source: UpdateProductRequest): UpdatePriceRequest = UpdatePriceRequest(
            currentPrice = CurrentPrice(value = source.currentPrice.value, currency_code = source.currentPrice.currency_code)
    )
}