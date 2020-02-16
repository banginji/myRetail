package com.myretail.service.handler

import com.myretail.service.domain.*
import com.myretail.service.mapper.*
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Function

@Service
class ProductHandler(val productPriceRepository: ProductPriceRepository) {

    private val host = "https://redsky.target.com"
    private val webClient: WebClient = WebClient.create(host)

    fun getProductInfo(request: ServerRequest) = Flux
            .combineLatest(
                    getProductPrice(request.pathVariable("id").toInt()),
                    getProductTitle(request.pathVariable("id").toInt()),
                    retrieveDataMapper()
            ).flatMap(getResponseMapper())
            .takeLast(1)
            .next()

    fun updateProductPrice(request: ServerRequest) = request
            .bodyToMono<ProductRequest>()
            .map(updateDataMapper())
            .flatMap(updateExistingProductPrice(request.pathVariable("id").toInt()))
            .onErrorResume(::badRequestResponse)

    private fun getProductPrice(id: Int) =
            findProductPriceById(id).flatMap(productPriceResponseMapper())
                    .switchIfEmpty(productPriceResponseMapper().apply(null))

    private fun getProductTitle(id: Int) = Flux
            .interval(Duration.ofMillis(200))
            .flatMap(invokeRedSkyCall(id))
            .retryBackoff(3, Duration.ofMillis(100))
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

    private fun findProductPriceById(id: Int) = productPriceRepository.findById(id)

    private fun updateProductPrice(current_price: CurrentPrice) = Function<ProductPrice, Mono<ProductPrice>> { productPrice ->
        productPriceRepository.save(
                ProductPrice(
                        productPrice.id,
                        current_price.value ?: productPrice.value,
                        current_price.currency_code ?: productPrice.currency_code
                )
        )
    }

    private fun updateExistingProductPrice(id: Int) = Function<ProductPriceRequest, Mono<ServerResponse>> { (current_price) ->
        findProductPriceById(id)
                .flatMap(updateProductPrice(current_price))
                .then(ok().build())
                .switchIfEmpty(status(HttpStatus.NOT_FOUND).build())
    }

    internal fun redSkyError(throwable: Throwable) =
            Mono.just(RedSkyResponse(null, RedSkyError("could not retrieve title from redsky: (${throwable.message})")))

    internal fun badRequestResponse(throwable: Throwable) =
            badRequest().body<ProductError>(Mono.just(ProductError("bad request")))
}

