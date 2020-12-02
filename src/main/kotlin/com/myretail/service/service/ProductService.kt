package com.myretail.service.service

import com.myretail.service.converter.ProductResponseConverter
import com.myretail.service.domain.ProductError
import com.myretail.service.domain.ProductResponse
import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.mapper.updateDataMapper
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.util.function.Function

@Service
class ProductService(
        private val priceService: PriceService,
        private val redSkyService: RedSkyService,
        private val productResponseConverter: ProductResponseConverter
) {
    @ExperimentalCoroutinesApi
    suspend fun getProductInfo(id: Int): ProductResponse = productResponseConverter.convert(priceService.getProductPrice(id) to redSkyService.getProductTitle(id))

    fun updateProductPrice(id: Int) = Function<Mono<UpdateProductRequest>, Mono<ServerResponse>> {
        it.map(updateDataMapper())
                .flatMap(priceService.updateProductPrice(id))
                .onErrorResume(::badRequestResponse)
    }

    fun badRequestResponse(throwable: Throwable?) =
            ServerResponse.badRequest().body<ProductError>(Mono.just(ProductError("bad request")))
}