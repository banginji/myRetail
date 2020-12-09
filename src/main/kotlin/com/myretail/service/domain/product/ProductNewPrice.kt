package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Entity that allows the user to specify a new value and currency code for a product that needs to be updated")
data class ProductNewPrice(@GraphQLDescription("New value of the product") val value: Double?, @GraphQLDescription("New currency code of the product") val currencyCode: String?)
