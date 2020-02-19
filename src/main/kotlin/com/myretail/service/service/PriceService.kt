package com.myretail.service.service

import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.UpdateProductPriceRequest
import com.myretail.service.mapper.productPriceResponseMapper
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Mono
import java.util.function.Function

@Service
class PriceService(val productPriceRepository: ProductPriceRepository) {
    fun getProductPrice(id: Int) =
            findProductPriceById(id).flatMap(productPriceResponseMapper())
                    .switchIfEmpty(productPriceResponseMapper().apply(null))

    fun updateProductPrice(id: Int) = Function<UpdateProductPriceRequest, Mono<ServerResponse>>{ updateExistingProductPrice(id, it) }

    fun updateExistingProductPrice(id: Int, updateProductPriceRequest: UpdateProductPriceRequest): Mono<ServerResponse> =
            findProductPriceById(id)
                    .flatMap(updateProductPrice(updateProductPriceRequest.current_price))
                    .switchIfEmpty(status(HttpStatus.NOT_FOUND).build())

    private fun findProductPriceById(id: Int) = productPriceRepository.findById(id)

    private fun updateProductPrice(current_price: CurrentPrice) = Function<ProductPrice, Mono<ServerResponse>> { productPrice ->
        productPriceRepository.save(
                ProductPrice(
                        productPrice.id,
                        current_price.value ?: productPrice.value,
                        current_price.currency_code ?: productPrice.currency_code
                )
        ).then(ok().build())
    }
}