package com.myretail.service.converter

import com.myretail.service.domain.price.NewPrice
import com.myretail.service.domain.product.UpdateProductRequest
import com.myretail.service.domain.price.UpdatePriceRequest
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UpdateRequestConverter : Converter<UpdateProductRequest, UpdatePriceRequest> {
    override fun convert(source: UpdateProductRequest): UpdatePriceRequest = UpdatePriceRequest(
            newPrice = NewPrice(value = source.newPrice.value, currencyCode = source.newPrice.currencyCode)
    )
}