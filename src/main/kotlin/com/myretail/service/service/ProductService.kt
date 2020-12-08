package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.domain.product.ProductResponse
import com.myretail.service.domain.product.UpdateProductRequest
import org.springframework.stereotype.Service

@Service
class ProductService(
        private val priceService: PriceService,
        private val redSkyService: RedSkyService,
        private val priceResponseConverter: PriceResponseConverter,
        private val redSkyResponseConverter: RedSkyResponseConverter,
        private val updateRequestConverter: UpdateRequestConverter,
) {
    suspend fun getProductInfo(id: Int): ProductResponse = ProductResponse(id, priceService, redSkyService, redSkyResponseConverter, priceResponseConverter)

    suspend fun updateProductPrice(id: Int, updateProductRequest: UpdateProductRequest) = priceService.updateProductPrice(id, updateRequestConverter.convert(updateProductRequest))
}