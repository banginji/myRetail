package com.myretail.service.graphql

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.myretail.service.service.ProductService

class ProductQuery(private val productService: ProductService) {
    @GraphQLDescription("Gets the product information")
    fun getProductInfo(@GraphQLDescription("The id of the product provided by the user for retrieval of information") id: Int) = productService.getProductInfo(id)
}