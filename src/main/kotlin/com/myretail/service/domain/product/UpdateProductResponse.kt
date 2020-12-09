package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Entity that displays the updated product's price response details")
data class UpdateProductResponse(@GraphQLDescription("Displays the product's current price details and errors if any on its retrieval") val price: ProductPrice?)
