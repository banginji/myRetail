package com.myretail.service.handler

import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.service.ProductService
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Mono

@Service
class ProductHandler(val productService: ProductService) {

    fun getProductInfo(request: ServerRequest): Mono<ServerResponse> = ok().build()

    fun updateProductPrice(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id").toIntOrNull() ?: -1

        return request
                .bodyToMono<UpdateProductRequest>()
                .transform(productService.updateProductPrice(id))
    }
}

