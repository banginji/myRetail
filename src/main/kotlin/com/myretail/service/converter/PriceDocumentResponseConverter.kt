package com.myretail.service.converter

import com.myretail.service.domain.price.Price
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.persistence.PriceDocument
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class PriceDocumentResponseConverter : Converter<PriceDocument, PriceResponse> {
    override fun convert(source: PriceDocument): PriceResponse = PriceResponse(Price(source.id, source.value, source.currency_code))
}