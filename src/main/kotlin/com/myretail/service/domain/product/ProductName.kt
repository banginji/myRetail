package com.myretail.service.domain.product

import com.expediagroup.graphql.annotations.GraphQLDescription

@GraphQLDescription("Entity that gives a product's name along with any errors if any on an attempt of its retrieval")
data class ProductName(@GraphQLDescription("Name of the product") val name: String?, @GraphQLDescription("Error message if there was an error in retrieving the name") val error: String?)
