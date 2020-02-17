package com.myretail.service.handler

import com.myretail.service.domain.ProductError
import com.myretail.service.domain.ProductRequest
import com.myretail.service.mapper.getResponseMapper
import com.myretail.service.mapper.retrieveDataMapper
import com.myretail.service.mapper.updateDataMapper
import com.myretail.service.service.ProductService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ProductHandler(val productService: ProductService) {

    fun getProductInfo(request: ServerRequest): Mono<ServerResponse> {
        val (productPriceResponse, redSkyResponse) =
                productService.getProductInfo(request.pathVariable("id").toInt())

        return Flux
                .combineLatest(productPriceResponse, redSkyResponse, retrieveDataMapper())
                .flatMap(getResponseMapper())
                .takeLast(1)
                .next()
    }

    fun updateProductPrice(request: ServerRequest) = request
            .bodyToMono<ProductRequest>()
            .map(updateDataMapper())
            .flatMap(productService.updateProductPrice(request.pathVariable("id").toInt()))
            .onErrorResume(::badRequestResponse)

    internal fun badRequestResponse(throwable: Throwable) =
            badRequest().body<ProductError>(Mono.just(ProductError("bad request")))
}

