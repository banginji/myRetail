package com.myretail.service.converter

import com.myretail.service.domain.ProductResponse
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.redsky.RedSkyResponse
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class ProductResponseConverter : Converter<Pair<ProductPriceResponse, RedSkyResponse>, ProductResponse> {
    override fun convert(
            source: Pair<ProductPriceResponse, RedSkyResponse>
    ): ProductResponse = ProductResponse(
            source.first.productPrice?.id ?: source.second.product?.item?.tcin?.toInt(),
            source.second.product?.item?.product_description?.title,
            source.first.productPrice?.let { CurrentPrice(source.first.productPrice?.value, source.first.productPrice?.currency_code) },
            listOfNotNull(source.first.productPriceError, source.second.redSkyError)
    )
}