package com.myretail.service

import com.myretail.service.domain.*
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPriceError
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.redsky.*
import com.myretail.service.handler.ProductHandler
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.service.PriceService
import com.myretail.service.service.RedSkyService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.function.Function

class ProductHandlerTest {
    private lateinit var priceService: PriceService
    private lateinit var redSkyService: RedSkyService

    private lateinit var productHandler: ProductHandler

    @BeforeEach
    fun beforeEach() {
        priceService = Mockito.mock(PriceService::class.java)
        redSkyService = Mockito.mock(RedSkyService::class.java)
        productHandler = Mockito.spy(ProductHandler(priceService, redSkyService))
    }

    @Test
    fun getProductInfo_forSuccessfulAggregationOfDataAcrossMultipleSources() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun getProductInfo_forSuccessfulAggregationFromAtLeastOneSource() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))

        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun getProductInfo_forFailureToObtainFromTwoSources() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))

        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val redSkyResponse = RedSkyResponse(null, RedSkyError("redsky error"))

        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }

    @Test
    fun updateProductPrice() {
        val id = 1

        Mockito
                .`when`(priceService.updateProductPrice(id))
                .thenReturn(Function { ok().build() })

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = UpdateProductRequest(CurrentPrice(newValue, newCurrencyCode))

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).body(Mono.just(updateProductRequest))

        StepVerifier
                .create(productHandler.updateProductPrice(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }
}