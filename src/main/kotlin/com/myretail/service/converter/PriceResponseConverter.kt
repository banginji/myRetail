package com.myretail.service.converter

import com.myretail.service.domain.price.ProductPrice
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.persistence.ProductPriceDocument
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class PriceResponseConverter : Converter<ProductPriceDocument, ProductPriceResponse> {
    override fun convert(source: ProductPriceDocument): ProductPriceResponse = ProductPriceResponse(ProductPrice(source.id, source.value, source.currency_code))
}