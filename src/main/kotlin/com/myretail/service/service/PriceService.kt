package com.myretail.service.service

import com.myretail.service.converter.PriceDocumentResponseConverter
import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.persistence.PriceDocument
import com.myretail.service.repository.PriceRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Service

@Service
class PriceService(
        private val priceRepository: PriceRepository,
        private val priceDocumentResponseConverter: PriceDocumentResponseConverter
) {
    suspend fun getProductPrice(id: Int): PriceResponse = findProductPriceById(id).map { priceDocumentResponseConverter.convert(it) }.awaitFirstOrElse { priceNotFound() }

    suspend fun updateProductPrice(
            id: Int, updatePriceRequest: UpdatePriceRequest
    ) : PriceResponse = findProductPriceById(id).flatMap { updateExistingProductPrice(it, updatePriceRequest.current_price) }.awaitFirstOrElse { priceNotFound() }

    fun findProductPriceById(id: Int) = priceRepository.findById(id)

    private fun updateExistingProductPrice(
            price: PriceDocument,
            currentPrice: CurrentPrice
    ) = saveProductPrice(price, currentPrice).map { priceDocumentResponseConverter.convert(it) }

    private fun saveProductPrice(
            price: PriceDocument,
            currentPrice: CurrentPrice
    ) = priceRepository.save(
            PriceDocument(
                    price.id,
                    currentPrice.value ?: price.value,
                    currentPrice.currency_code ?: price.currency_code
            )
    )

    private fun priceNotFound() = PriceResponse(null, ProductError("price not found in data store"))
}