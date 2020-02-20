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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.util.function.Function

@WebFluxTest
class ProductHttpTest {
    @MockBean
    private lateinit var productPriceRepository: ProductPriceRepository

    private lateinit var priceService: PriceService

    private lateinit var redSkyService: RedSkyService

    private lateinit var productService: ProductService

    private lateinit var client: WebTestClient
    private lateinit var productHandler: ProductHandler

    @BeforeEach
    fun beforeEach() {
        priceService = Mockito.spy(PriceService(productPriceRepository))
        redSkyService = Mockito.spy(RedSkyService())
        productService = Mockito.spy(ProductService(priceService, redSkyService))
        productHandler = Mockito.spy(ProductHandler(productService))
        client = WebTestClient.bindToRouterFunction(routes(productHandler)).build()
    }

    @Test
    fun `get product response when data exists in data store and redsky`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val title = "item1"

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(ProductPriceDocument(id, value, currencyCode)))

        Mockito
                .doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.just(RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)) })
                .`when`(redSkyService).invokeRedSkyCall(id)

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

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(ProductPriceDocument(id, value, currencyCode)))

        Mockito
                .doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) })
                .`when`(redSkyService).invokeRedSkyCall(id)

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

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty())

        Mockito
                .doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.just(RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)) })
                .`when`(redSkyService).invokeRedSkyCall(id)

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

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty())

        Mockito
                .doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) })
                .`when`(redSkyService).invokeRedSkyCall(id)

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
