package com.myretail.service.service

import com.myretail.service.domain.ProductError
import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.mapper.getResponseMapper
import com.myretail.service.mapper.retrieveDataMapper
import com.myretail.service.mapper.updateDataMapper
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.function.Function

@Service
class ProductService(val priceService: PriceService, val redSkyService: RedSkyService) {
    fun getProductInfo(id: Int) = Flux
            .combineLatest(
                    priceService.getProductPrice(id),
                    redSkyService.getProductTitle(id),
                    retrieveDataMapper()
            )
            .flatMap(getResponseMapper())
            .takeLast(1)
            .next()
            .onErrorResume(::badRequestResponse)

    fun updateProductPrice(id: Int) = Function<Mono<UpdateProductRequest>, Mono<ServerResponse>> {
        it.map(updateDataMapper())
                .flatMap(priceService.updateProductPrice(id))
                .onErrorResume(::badRequestResponse)
    }

    fun badRequestResponse(throwable: Throwable?) =
            ServerResponse.badRequest().body<ProductError>(Mono.just(ProductError("bad request")))
}