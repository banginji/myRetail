package com.myretail.service.handler

import com.myretail.service.domain.*
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import org.bson.json.JsonParseException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.*
import org.springframework.web.reactive.function.server.body
import org.springframework.web.reactive.function.server.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.BiFunction
import java.util.function.Function

@Service
class ProductHandler(val productPriceRepository: ProductPriceRepository) {

    private val host = "https://redsky.target.com"
    private val webClient: WebClient = WebClient.create(host)

    fun getProductInfo(request: ServerRequest) = Flux
            .combineLatest(
                    getPrice(request.pathVariable("id").toInt()),
                    getProductTitle(request.pathVariable("id").toInt()),
                    retrieveDataMapper()
            ).flatMap { ok().body<ProductResponse>(Mono.just(it)) }
            .takeLast(1)
            .next()

    fun updateProductPrice(request: ServerRequest) = request
            .bodyToMono<ProductRequest>()
            .map(updateDataMapper())
            .flatMap { (current_price) ->
                productPriceRepository.findById(request.pathVariable("id").toInt())
                        .flatMap { productPrice ->
                            productPriceRepository.save(
                                    ProductPrice(
                                            productPrice.id,
                                            current_price.value?.let { it } ?: productPrice.value,
                                            current_price.currency_code?.let { it } ?: productPrice.currency_code
                                    )
                            )
                        }
                        .then(ok().build())
                        .switchIfEmpty(status(HttpStatus.NOT_FOUND).build())
            }
            .onErrorResume(::badRequestResponse)

    private fun getPrice(id: Int) =
            productPriceRepository.findById(id).flatMap { Mono.just(ProductPriceResponse(it)) }
                    .switchIfEmpty(Mono.just(ProductPriceResponse(null, ProductPriceError("price not found in data store"))))

    private fun getProductTitle(id: Int) = Flux
            .interval(Duration.ofMillis(200))
            .flatMap {
                webClient
                        .get()
                        .uri("/v2/pdp/tcin/$id?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics")
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError) { Mono.empty() }
                        .bodyToMono<RedSkyResponse>()
            }
            .retryBackoff(3, Duration.ofMillis(100))
            .take(1)
            .next()
            .onErrorResume(::redSkyError)

    internal fun retrieveDataMapper() = BiFunction<ProductPriceResponse, RedSkyResponse, ProductResponse> { productPriceResponse, redSkyResponse ->
        ProductResponse(
                productPriceResponse.productPrice?.let { it.id } ?: redSkyResponse.product?.item?.tcin?.toInt(),
                redSkyResponse.product?.item?.product_description?.title,
                productPriceResponse.productPrice?.let { CurrentPrice(productPriceResponse.productPrice.value, productPriceResponse.productPrice.currency_code) },
                listOfNotNull(productPriceResponse.productPriceError, redSkyResponse.redSkyError)
        )
    }

    internal fun updateDataMapper() = Function<ProductRequest, ProductPriceRequest> { (current_price) -> ProductPriceRequest(current_price) }

    internal fun redSkyError(throwable: Throwable) =
            Mono.just(RedSkyResponse(null, RedSkyError("could not retrieve title from redsky: (${throwable.message})")))

    internal fun badRequestResponse(throwable: Throwable) =
            badRequest().body<ProductError>(Mono.just(ProductError("bad request")))
}

