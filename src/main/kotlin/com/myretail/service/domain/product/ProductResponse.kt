package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.service.PriceService
import com.myretail.service.service.RedSkyService

@GraphQLDescription("Entity that gives a product's name and price retrieved")
class ProductResponse(
    @GraphQLDescription("The id of the product to be retrieved") val id: Int,
    @GraphQLIgnore private val priceService: PriceService,
    @GraphQLIgnore private val redSkyService: RedSkyService,
    @GraphQLIgnore private val redSkyResponseConverter: RedSkyResponseConverter,
    @GraphQLIgnore private val priceResponseConverter: PriceResponseConverter,
) {
    @GraphQLDescription("Gets the name of the product along with errors if any")
    suspend fun name(): ProductName = redSkyResponseConverter.convert(redSkyService.getProductTitle(id))

    @GraphQLDescription("Gets the price of the product")
    suspend fun price(): ProductPrice = priceResponseConverter.convert(priceService.getProductPrice(id))
}