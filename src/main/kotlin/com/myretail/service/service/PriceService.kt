package com.myretail.service.service

import com.myretail.service.converter.PriceDocumentResponseConverter
import com.myretail.service.converter.UpdatePriceDocumentConverter
import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.price.NewPrice
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.persistence.PriceDocument
import com.myretail.service.repository.PriceRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.stereotype.Service

@Service
class PriceService(
        private val priceRepository: PriceRepository,
        private val priceDocumentResponseConverter: PriceDocumentResponseConverter,
        private val updatePriceDocumentConverter: UpdatePriceDocumentConverter
) {
    suspend fun getProductPrice(id: Int): PriceResponse = findProductPriceById(id).map { priceDocumentResponseConverter.convert(it) }.awaitFirstOrElse { priceNotFound() }

    suspend fun updateProductPrice(
            id: Int, updatePriceRequest: UpdatePriceRequest
    ) : PriceResponse = findProductPriceById(id).flatMap { updateExistingProductPrice(it, updatePriceRequest.newPrice) }.awaitFirstOrElse { priceNotFound() }

    fun findProductPriceById(id: Int) = priceRepository.findById(id)

    private fun updateExistingProductPrice(
        price: PriceDocument,
        newPrice: NewPrice
    ) = saveProductPrice(price, newPrice).map { priceDocumentResponseConverter.convert(it) }

    private fun saveProductPrice(
        price: PriceDocument,
        newPrice: NewPrice
    ) = priceRepository.save(updatePriceDocumentConverter.convert(price to newPrice))

    private fun priceNotFound() = PriceResponse(null, ProductError("price not found in data store"))
}