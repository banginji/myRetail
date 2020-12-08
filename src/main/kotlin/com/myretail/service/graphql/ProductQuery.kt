package com.myretail.service.graphql

import com.myretail.service.service.ProductService

class ProductQuery(private val productService: ProductService) {
    suspend fun getProductInfo(id: Int) = productService.getProductInfo(id)
}