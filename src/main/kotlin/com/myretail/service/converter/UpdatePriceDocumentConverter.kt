package com.myretail.service.converter

import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.persistence.PriceDocument
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class UpdatePriceDocumentConverter: Converter<Pair<PriceDocument, CurrentPrice>, PriceDocument>{
    override fun convert(
        source: Pair<PriceDocument, CurrentPrice>
    ): PriceDocument = PriceDocument(
        source.first.id,
        source.second.value ?: source.first.value,
        source.second.currencyCode ?: source.first.currency_code
    )
}