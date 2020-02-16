package com.myretail.service.mapper

import com.myretail.service.domain.*
import java.util.function.BiFunction
import java.util.function.Function

fun retrieveDataMapper() = BiFunction<ProductPriceResponse, RedSkyResponse, ProductResponse> { productPriceResponse, redSkyResponse ->
    ProductResponse(
            productPriceResponse.productPrice?.id ?: redSkyResponse.product?.item?.tcin?.toInt(),
            redSkyResponse.product?.item?.product_description?.title,
            productPriceResponse.productPrice?.let { CurrentPrice(productPriceResponse.productPrice.value, productPriceResponse.productPrice.currency_code) },
            listOfNotNull(productPriceResponse.productPriceError, redSkyResponse.redSkyError)
    )
}

fun updateDataMapper() = Function<ProductRequest, ProductPriceRequest> { (current_price) -> ProductPriceRequest(current_price) }