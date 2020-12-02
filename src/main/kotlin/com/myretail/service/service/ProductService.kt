package com.myretail.service.service

import com.myretail.service.converter.ProductResponseConverter
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.domain.ProductResponse
import com.myretail.service.domain.UpdateProductRequest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.stereotype.Service

@Service
class ProductService(
        private val priceService: PriceService,
        private val redSkyService: RedSkyService,
        private val productResponseConverter: ProductResponseConverter,
        private val updateRequestConverter: UpdateRequestConverter
) {
    @ExperimentalCoroutinesApi
    suspend fun getProductInfo(id: Int): ProductResponse = productResponseConverter.convert(priceService.getProductPrice(id) to redSkyService.getProductTitle(id))

    suspend fun updateProductPrice(id: Int, updateProductRequest: UpdateProductRequest) = priceService.updateProductPrice(id, updateRequestConverter.convert(updateProductRequest))
}