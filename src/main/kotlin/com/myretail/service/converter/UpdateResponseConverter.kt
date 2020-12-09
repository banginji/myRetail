package com.myretail.service.converter

import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.product.ProductCurrentPrice
import com.myretail.service.domain.product.ProductPrice
import com.myretail.service.domain.product.UpdateProductResponse
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UpdateResponseConverter: Converter<PriceResponse, UpdateProductResponse> {
    override fun convert(source: PriceResponse): UpdateProductResponse = UpdateProductResponse(
            price = ProductPrice(
                    currentPrice = ProductCurrentPrice(
                            value = source.price?.value,
                            currency_code = source.price?.currency_code
                    ),
                    error = source.error?.error
            )
    )
}