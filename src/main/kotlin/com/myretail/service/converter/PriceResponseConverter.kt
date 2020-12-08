package com.myretail.service.converter

import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.product.ProductCurrentPrice
import com.myretail.service.domain.product.ProductPrice
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class PriceResponseConverter: Converter<PriceResponse, ProductPrice> {
    override fun convert(source: PriceResponse): ProductPrice? = ProductPrice(
            currentPrice = source.price?.let { ProductCurrentPrice(source.price.value, source.price.currency_code) },
            error = source.productPriceError?.error
    )
}