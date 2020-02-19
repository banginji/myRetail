package com.myretail.service.handler

import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.service.ProductViewService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Service
class ProductHandler(val productViewService: ProductViewService) {

    fun getProductInfo(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id").toIntOrNull() ?: -1

        return productViewService.getProductInfo(id)
    }

    fun updateProductPrice(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id").toIntOrNull() ?: -1

        return request
                .bodyToMono<UpdateProductRequest>()
                .transform(productViewService.updateProductPrice(id))
    }
}

