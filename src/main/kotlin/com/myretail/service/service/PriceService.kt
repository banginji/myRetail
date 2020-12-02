package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.domain.ProductError
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.price.UpdateProductPriceRequest
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import kotlinx.coroutines.reactive.awaitFirstOrElse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import java.util.function.Function

@Service
class PriceService(
        private val productPriceRepository: ProductPriceRepository,
        private val priceResponseConverter: PriceResponseConverter
) {
    suspend fun getProductPrice(id: Int): ProductPriceResponse = findProductPriceById(id).map { priceResponseConverter.convert(it) }.awaitFirstOrElse { priceNotFound() }

    private fun priceNotFound() = ProductPriceResponse(null, ProductError("price not found in data store"))

    fun updateProductPrice(id: Int) = Function<UpdateProductPriceRequest, Mono<ServerResponse>> { updateExistingProductPrice(id, it) }

    fun updateExistingProductPrice(id: Int, updateProductPriceRequest: UpdateProductPriceRequest) =
            findProductPriceById(id)
                    .flatMap(updateProductPrice(updateProductPriceRequest.current_price))
                    .switchIfEmpty(status(HttpStatus.NOT_FOUND).build())

    private fun findProductPriceById(id: Int) = productPriceRepository.findById(id)

    private fun updateProductPrice(current_price: CurrentPrice) = Function<ProductPriceDocument, Mono<ServerResponse>> { productPrice ->
        productPriceRepository.save(
                ProductPriceDocument(
                        productPrice.id,
                        current_price.value ?: productPrice.value,
                        current_price.currency_code ?: productPrice.currency_code
                )
        ).then(ok().build())
    }
}