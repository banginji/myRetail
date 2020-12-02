package com.myretail.service

import com.myretail.service.config.routes
import com.myretail.service.domain.redsky.RedSkyProduct
import com.myretail.service.domain.redsky.RedSkyProductItem
import com.myretail.service.domain.redsky.RedSkyProductItemDesc
import com.myretail.service.domain.redsky.RedSkyResponse
import com.myretail.service.handler.ProductHandler
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import com.myretail.service.service.PriceService
import com.myretail.service.service.ProductService
import com.myretail.service.service.RedSkyService
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.context.annotation.Import
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.function.Function

@WebFluxTest
@Import(ProductHandler::class, ProductService::class, PriceService::class)
class ProductIntegrationTest(@Autowired private val productHandler: ProductHandler) {
    @MockkBean
    private lateinit var productPriceRepository: ProductPriceRepository

    @SpykBean
    private lateinit var redSkyService: RedSkyService

    private lateinit var client: WebTestClient

    @BeforeEach
    fun beforeEach() {
        client = WebTestClient.bindToRouterFunction(routes(productHandler)).build()
    }

    @Test
    fun `get product response when data exists in data store and redsky`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val title = "item1"

        every { productPriceRepository.findById(id) } returns Mono.just(ProductPriceDocument(id, value, currencyCode))

        every { redSkyService.invokeRedSkyCall(id) } returns Function { Mono.just(RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)) }

        /**
         * {
            "id": 13860428,
            "name": "The Big Lebowski (Blu-ray)",
            "current_price": {
                "value": 1193.33,
                "currency_code": "USD"
            },
            "productErrors": []
        }
         */

        client
                .get()
                .uri("/product/$id")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo(title)
                .jsonPath("$.current_price").exists()
                .jsonPath("$.current_price.value").isEqualTo(value)
                .jsonPath("$.current_price.currency_code").isEqualTo(currencyCode)
                .jsonPath("$.productErrors").isArray
                .jsonPath("$.productErrors.length()").isEqualTo(0)
    }

    @Test
    fun `get product response when data exists in data store but not in redsky`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"

        val redSkyErrorMessage = "could not retrieve title from redsky: (Retries exhausted: 3/3)"

        every { productPriceRepository.findById(id) } returns Mono.just(ProductPriceDocument(id, value, currencyCode))

        every { redSkyService.invokeRedSkyCall(id) } returns Function { Mono.error(Throwable()) }

        /**
         * {
            "id": 234,
            "current_price": {
                "value": 74.24,
                "currency_code": "USD"
            },
            "productErrors": [
                {
                    "redSkyError": "could not retrieve title from redsky: (Retries exhausted: 3/3)"
                }
            ]
        }
         */

        client
                .get()
                .uri("/product/$id")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").doesNotExist()
                .jsonPath("$.current_price").exists()
                .jsonPath("$.current_price.value").isEqualTo(value)
                .jsonPath("$.current_price.currency_code").isEqualTo(currencyCode)
                .jsonPath("$.productErrors").isArray
                .jsonPath("$.productErrors.length()").isEqualTo(1)
                .jsonPath("$.productErrors.[0].redSkyError").exists()
                .jsonPath("$.productErrors.[0].redSkyError").isEqualTo(redSkyErrorMessage)
    }

    @Test
    fun `get product response when data not exist in data store but exists in redsky`() {
        val id = 8
        val title = "item1"
        val productPriceError = "price not found in data store"

        every { productPriceRepository.findById(id) } returns Mono.empty()

        every { redSkyService.invokeRedSkyCall(id) } returns Function<Long, Mono<RedSkyResponse>> { Mono.just(RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)) }

        /**
         * {
            "id": 13860427,
            "name": "Conan the Barbarian (dvd_video)",
            "productErrors": [
                {
                    "productPriceError": "price not found in data store"
                }
            ]
        }
         */

        client
                .get()
                .uri("/product/$id")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo(title)
                .jsonPath("$.current_price").doesNotExist()
                .jsonPath("$.productErrors").isArray
                .jsonPath("$.productErrors.length()").isEqualTo(1)
                .jsonPath("$.productErrors.[0].productPriceError").exists()
                .jsonPath("$.productErrors.[0].productPriceError").isEqualTo(productPriceError)
    }

    @Test
    fun `get product response when data not exist in both data store and redsky`() {
        val id = 8

        val redSkyErrorMessage = "could not retrieve title from redsky: (Retries exhausted: 3/3)"
        val productPriceError = "price not found in data store"

        every { productPriceRepository.findById(id) } returns Mono.empty()

        every { redSkyService.invokeRedSkyCall(id) } returns Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) }

        /**
         * {
            "productErrors": [
                {
                    "productPriceError": "price not found in data store"
                },
                {
                    "redSkyError": "could not retrieve title from redsky: (Retries exhausted: 3/3)"
                }
            ]
        }
         */

        client
                .get()
                .uri("/product/$id")
                .exchange()
                .expectStatus().isNotFound
                .expectBody()
                .jsonPath("$").exists()
                .jsonPath("$.id").doesNotExist()
                .jsonPath("$.name").doesNotExist()
                .jsonPath("$.current_price").doesNotExist()
                .jsonPath("$.productErrors").isArray
                .jsonPath("$.productErrors.length()").isEqualTo(2)
                .jsonPath("$.productErrors.[0].productPriceError").exists()
                .jsonPath("$.productErrors.[0].productPriceError").isEqualTo(productPriceError)
                .jsonPath("$.productErrors.[1].redSkyError").exists()
                .jsonPath("$.productErrors.[1].redSkyError").isEqualTo(redSkyErrorMessage)
    }
}
