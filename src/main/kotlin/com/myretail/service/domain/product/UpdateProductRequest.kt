package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Entity that allows the user to specify the values they want to modify for a product")
data class UpdateProductRequest(@GraphQLDescription("Values to be provided by the user for updating price details of the product") val newPrice: ProductNewPrice)