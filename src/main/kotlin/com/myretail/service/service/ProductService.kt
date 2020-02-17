package com.myretail.service.service

import com.myretail.service.domain.CurrentPrice
import com.myretail.service.domain.ProductPriceRequest
import com.myretail.service.domain.RedSkyError
import com.myretail.service.domain.RedSkyResponse
import com.myretail.service.mapper.productPriceResponseMapper
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.ServerResponse.status
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Function

@Service
class ProductService(val productPriceRepository: ProductPriceRepository) {

    private val host = "https://redsky.target.com"
    private val webClient: WebClient = WebClient.create(host)

    fun getProductInfo(id: Int) = getProductPrice(id) to getProductTitle(id)

    fun updateProductPrice(id: Int) = Function<ProductPriceRequest, Mono<ServerResponse>>{ updateExistingProductPrice(id, it) }

    fun getProductPrice(id: Int) =
            findProductPriceById(id).flatMap(productPriceResponseMapper())
                    .switchIfEmpty(productPriceResponseMapper().apply(null))

    fun getProductTitle(id: Int) = Flux
            .interval(Duration.ofMillis(200))
            .flatMap(invokeRedSkyCall(id))
            .retryBackoff(3, Duration.ofMillis(100), Duration.ofSeconds(1), 0.1)
            .take(1)
            .next()
            .onErrorResume(::redSkyError)

    fun invokeRedSkyCall(id: Int) = Function<Long, Mono<RedSkyResponse>> {
        webClient
                .get()
                .uri("/v2/pdp/tcin/$id?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics")
                .retrieve()
                .bodyToMono()
    }

    fun findProductPriceById(id: Int) = productPriceRepository.findById(id)

    private fun updateProductPrice(current_price: CurrentPrice) = Function<ProductPrice, Mono<ServerResponse>> { productPrice ->
        productPriceRepository.save(
                ProductPrice(
                        productPrice.id,
                        current_price.value ?: productPrice.value,
                        current_price.currency_code ?: productPrice.currency_code
                )
        ).then(ok().build())
    }

    private fun updateExistingProductPrice(id: Int, productPriceRequest: ProductPriceRequest): Mono<ServerResponse> =
        findProductPriceById(id)
                .flatMap(updateProductPrice(productPriceRequest.current_price))
                .switchIfEmpty(status(HttpStatus.NOT_FOUND).build())

    private fun redSkyError(throwable: Throwable) =
            Mono.just(RedSkyResponse(null, RedSkyError("could not retrieve title from redsky: (${throwable.message})")))
}