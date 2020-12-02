package com.myretail.service.graphql

import com.myretail.service.domain.ProductResponse
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.service.PriceService
import com.myretail.service.service.RedSkyService
import kotlinx.coroutines.ExperimentalCoroutinesApi

class ProductQuery(private val priceService: PriceService, private val redSkyService: RedSkyService) {
    @ExperimentalCoroutinesApi
    suspend fun getProductInfo(id: Int): ProductResponse {
        val rsr = redSkyService.getProductTitle(id)
        val psr = priceService.getProductPrice(id)

        return ProductResponse(
                psr.productPrice?.id ?: rsr?.product?.item?.tcin?.toInt(),
                rsr?.product?.item?.product_description?.title,
                psr.productPrice?.let { CurrentPrice(psr.productPrice.value, psr.productPrice.currency_code) },
                listOfNotNull(psr.productPriceError, rsr?.redSkyError)
        )
    }
}