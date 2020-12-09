package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Entity that gives the product's price along with any errors if any that occurred during its retrieval")
data class ProductPrice(@GraphQLDescription("Gets the product's current value and currency code") val currentPrice: ProductCurrentPrice?, @GraphQLDescription("Error message if there was an error in retrieving the price") val error: String?)
