package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.converter.UpdateResponseConverter
import com.myretail.service.domain.product.ProductResponse
import com.myretail.service.domain.product.UpdateProductRequest
import com.myretail.service.domain.product.UpdateProductResponse
import org.springframework.stereotype.Service

@Service
class ProductService(
        private val priceService: PriceService,
        private val redSkyService: RedSkyService,
        private val priceResponseConverter: PriceResponseConverter,
        private val redSkyResponseConverter: RedSkyResponseConverter,
        private val updateRequestConverter: UpdateRequestConverter,
        private val updateResponseConverter: UpdateResponseConverter
) {
    fun getProductInfo(id: Int): ProductResponse = ProductResponse(id, priceService, redSkyService, redSkyResponseConverter, priceResponseConverter)

    fun updateProductPrice(id: Int, updateProductRequest: UpdateProductRequest) = UpdateProductResponse(id, updateProductRequest, priceService, updateRequestConverter, updateResponseConverter)
}