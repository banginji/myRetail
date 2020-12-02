package com.myretail.service.graphql

import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.service.ProductService

class ProductMutation(private val productService: ProductService) {
    suspend fun updateProductInfo(id: Int, updateProductRequest: UpdateProductRequest) = productService.updateProductPrice(id, updateProductRequest)
}