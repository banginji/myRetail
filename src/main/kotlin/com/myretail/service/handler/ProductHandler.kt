package com.myretail.service.handler

import com.myretail.service.domain.ProductError
import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.mapper.getResponseMapper
import com.myretail.service.mapper.retrieveDataMapper
import com.myretail.service.mapper.updateDataMapper
import com.myretail.service.service.PriceService
import com.myretail.service.service.RedSkyService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ProductHandler(val priceService: PriceService, val redSkyService: RedSkyService) {

    fun getProductInfo(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id").toIntOrNull() ?: -1

        return Flux
                .combineLatest(
                        priceService.getProductPrice(id),
                        redSkyService.getProductTitle(id),
                        retrieveDataMapper()
                )
                .flatMap(getResponseMapper())
                .takeLast(1)
                .next()
                .onErrorResume(::badRequestResponse)
    }

    fun updateProductPrice(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id").toIntOrNull() ?: -1

        return request
                .bodyToMono<UpdateProductRequest>()
                .map(updateDataMapper())
                .flatMap(priceService.updateProductPrice(id))
                .onErrorResume(::badRequestResponse)
    }

    fun badRequestResponse(throwable: Throwable?) =
            badRequest().body<ProductError>(Mono.just(ProductError("bad request")))
}

