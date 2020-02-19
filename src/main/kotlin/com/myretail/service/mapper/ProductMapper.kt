package com.myretail.service.mapper

import com.myretail.service.domain.*
import com.myretail.service.domain.price.*
import com.myretail.service.domain.redsky.RedSkyResponse
import com.myretail.service.persistence.ProductPriceDocument
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import java.util.function.BiFunction
import java.util.function.Function

fun retrieveDataMapper() = BiFunction<ProductPriceResponse, RedSkyResponse, ProductResponse> { productPriceResponse, redSkyResponse ->
    ProductResponse(
            productPriceResponse.productPrice?.id ?: redSkyResponse.product?.item?.tcin?.toInt(),
            redSkyResponse.product?.item?.product_description?.title,
            productPriceResponse.productPrice?.let { CurrentPrice(productPriceResponse.productPrice.value, productPriceResponse.productPrice.currency_code) },
            listOfNotNull(productPriceResponse.productPriceError, redSkyResponse.redSkyError)
    )
}

fun updateDataMapper() = Function<UpdateProductRequest, UpdateProductPriceRequest> { (current_price) -> UpdateProductPriceRequest(current_price) }

fun productPriceResponseMapper() = Function<ProductPriceDocument?, Mono<ProductPriceResponse>> {
    it
            ?.let { Mono.just(ProductPriceResponse(ProductPrice(it.id, it.value, it.currency_code))) }
            ?: Mono.just(ProductPriceResponse(null, ProductPriceError("price not found in data store")))
}

fun getResponseMapper() = Function<ProductResponse, Mono<ServerResponse>> {
    if (it.productErrors.size < 2)
        ok().body<ProductResponse>(Mono.just(it))
    else
        status(HttpStatus.NOT_FOUND).body<ProductResponse>(Mono.just(it))
}