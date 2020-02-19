package com.myretail.service

import com.myretail.service.domain.ProductResponse
import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.handler.ProductHandler
import com.myretail.service.service.ProductViewService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.function.Function

class ProductHandlerTest {
    private lateinit var productViewService: ProductViewService

    private lateinit var productHandler: ProductHandler

    @BeforeEach
    fun beforeEach() {
        productViewService = Mockito.mock(ProductViewService::class.java)
        productHandler = Mockito.spy(ProductHandler(productViewService))
    }

    @Test
    fun getProductInfo() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"
        val title = "item1"

        val productResponse = ProductResponse(id, title, CurrentPrice(value, currencyCode), emptyList())
        Mockito
                .`when`(productViewService.getProductInfo(id))
                .thenReturn(ok().body<ProductResponse>(Mono.just(productResponse)))

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun updateProductPrice() {
        val id = 1

        Mockito
                .`when`(productViewService.updateProductPrice(id))
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