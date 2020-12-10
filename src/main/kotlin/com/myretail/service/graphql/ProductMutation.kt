package com.myretail.service.graphql

import com.expediagroup.graphql.annotations.GraphQLDescription
import com.myretail.service.domain.product.UpdateProductRequest
import com.myretail.service.service.ProductService

class ProductMutation(private val productService: ProductService) {
    @GraphQLDescription("Enables the user to update a product's price")
    fun updateProductInfo(@GraphQLDescription("The id of the product provided by the user for retrieval of information") id: Int, @GraphQLDescription("Entity that contains the fields the user wants to update")  updateProductRequest: UpdateProductRequest) = productService.updateProductPrice(id, updateProductRequest)
}