package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.service.PriceService
import com.myretail.service.service.RedSkyService

class ProductResponse(
        val id: Int,
        @GraphQLIgnore private val priceService: PriceService,
        @GraphQLIgnore private val redSkyService: RedSkyService,
        @GraphQLIgnore private val redSkyResponseConverter: RedSkyResponseConverter,
        @GraphQLIgnore private val priceResponseConverter: PriceResponseConverter,
) {
    suspend fun name(): ProductName = redSkyResponseConverter.convert(redSkyService.getProductTitle(id))

    suspend fun price(): ProductPrice = priceResponseConverter.convert(priceService.getProductPrice(id))
}