package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.expediagroup.graphql.annotations.GraphQLIgnore
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.converter.UpdateResponseConverter
import com.myretail.service.service.PriceService

@GraphQLDescription("Entity that displays the updated product's price response details")
class UpdateProductResponse(
    @GraphQLIgnore private val id: Int,
    @GraphQLIgnore private val updateProductRequest: UpdateProductRequest,
    @GraphQLIgnore private val priceService: PriceService,
    @GraphQLIgnore private val updateRequestConverter: UpdateRequestConverter,
    @GraphQLIgnore private val updateResponseConverter: UpdateResponseConverter,
) {
    @GraphQLDescription("Displays the product's current price details and errors if any on its retrieval")
    suspend fun price() = updateResponseConverter.convert(priceService.updateProductPrice(id, updateRequestConverter.convert(updateProductRequest)))
}
