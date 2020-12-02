package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.domain.ProductError
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.price.UpdateProductPriceRequest
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Service

@Service
class PriceService(
        private val productPriceRepository: ProductPriceRepository,
        private val priceResponseConverter: PriceResponseConverter
) {
    suspend fun getProductPrice(id: Int): ProductPriceResponse = findProductPriceById(id).map { priceResponseConverter.convert(it) }.awaitFirstOrElse { priceNotFound() }

    suspend fun updateProductPrice(
            id: Int, updateProductPriceRequest: UpdateProductPriceRequest
    ) : ProductPriceResponse = findProductPriceById(id).flatMap { updateExistingProductPrice(it, updateProductPriceRequest.current_price) }.awaitFirstOrElse { priceNotFound() }

    fun findProductPriceById(id: Int) = productPriceRepository.findById(id)

    private fun updateExistingProductPrice(
            productPrice: ProductPriceDocument,
            currentPrice: CurrentPrice
    ) = saveProductPrice(productPrice, currentPrice).map { priceResponseConverter.convert(it) }

    private fun saveProductPrice(
            productPrice: ProductPriceDocument,
            currentPrice: CurrentPrice
    ) = productPriceRepository.save(
            ProductPriceDocument(
                    productPrice.id,
                    currentPrice.value ?: productPrice.value,
                    currentPrice.currency_code ?: productPrice.currency_code
            )
    )

    private fun priceNotFound() = ProductPriceResponse(null, ProductError("price not found in data store"))
}