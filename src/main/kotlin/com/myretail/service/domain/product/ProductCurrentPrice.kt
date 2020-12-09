package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Entity that gives the product's current value and currency code")
data class ProductCurrentPrice(@GraphQLDescription("Product's value") val value: Double?, @GraphQLDescription("Product's currency code") val currencyCode: String?)
