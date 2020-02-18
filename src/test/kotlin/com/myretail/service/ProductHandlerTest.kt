package com.myretail.service

import com.myretail.service.domain.*
import com.myretail.service.handler.ProductHandler
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.service.ProductService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.function.Function

class ProductHandlerTest {
    private lateinit var productService: ProductService

    private lateinit var productHandler: ProductHandler

    @BeforeEach
    fun beforeEach() {
        productService = Mockito.mock(ProductService::class.java)
        productHandler = Mockito.spy(ProductHandler(productService))
    }

    @Test
    fun `getProductInfo_forSuccessfulAggregationOfDataAcrossMultipleSources`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Pair(Mono.just(productPriceResponse), Mono.just(redSkyResponse))).`when`(productService).getProductInfo(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `getProductInfo_forSuccessfulAggregationFromAtLeastOneSource`() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Pair(Mono.just(productPriceResponse), Mono.just(redSkyResponse))).`when`(productService).getProductInfo(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `getProductInfo_forFailureToObtainFromTwoSources`() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))

        val redSkyResponse = RedSkyResponse(null, RedSkyError("redsky error"))

        Mockito.doReturn(Pair(Mono.just(productPriceResponse), Mono.just(redSkyResponse))).`when`(productService).getProductInfo(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }

    @Test
    fun `updateProductPrice`() {
        val id = 1

        Mockito
                .`when`(productService.updateProductPrice(id))
                .thenReturn(Function { ok().build() })

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = ProductRequest(CurrentPrice(newValue, newCurrencyCode))

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).body(Mono.just(updateProductRequest))

        StepVerifier
                .create(productHandler.updateProductPrice(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }
}