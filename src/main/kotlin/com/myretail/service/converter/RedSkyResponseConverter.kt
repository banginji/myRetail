package com.myretail.service.converter

import com.myretail.service.domain.product.ProductName
import com.myretail.service.domain.redsky.RedSkyResponse
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class RedSkyResponseConverter: Converter<RedSkyResponse, ProductName> {
    override fun convert(source: RedSkyResponse): ProductName = ProductName(name = source.product?.item?.product_description?.title, error = source.redSkyError?.error)
}