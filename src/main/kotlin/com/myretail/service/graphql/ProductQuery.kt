package com.myretail.service.graphql

import com.myretail.service.service.ProductService
import kotlinx.coroutines.ExperimentalCoroutinesApi

class ProductQuery(private val productService: ProductService) {
    @ExperimentalCoroutinesApi
    suspend fun getProductInfo(id: Int) = productService.getProductInfo(id)
}