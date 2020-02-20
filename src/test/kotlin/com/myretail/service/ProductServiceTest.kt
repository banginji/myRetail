package com.myretail.service

import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPrice
import com.myretail.service.domain.price.ProductPriceError
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.redsky.*
import com.myretail.service.service.PriceService
import com.myretail.service.service.ProductService
import com.myretail.service.service.RedSkyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.function.Function

class ProductServiceTest {
    private lateinit var priceService: PriceService
    private lateinit var redSkyService: RedSkyService

    private lateinit var productService: ProductService

    @BeforeEach
    fun beforeEach() {
        priceService = Mockito.mock(PriceService::class.java)
        redSkyService = Mockito.mock(RedSkyService::class.java)
        productService = Mockito.spy(ProductService(priceService, redSkyService))
    }

    @Test
    fun `getProductInfo for successful aggregation of data across multiple sources`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        StepVerifier
                .create(productService.getProductInfo(id))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `getProductInfo for successful aggregation from at least one source`() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))

        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        StepVerifier
                .create(productService.getProductInfo(id))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `getProductInfo for failure to obtain from two sources`() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))

        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val redSkyResponse = RedSkyResponse(null, RedSkyError("redsky error"))

        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        StepVerifier
                .create(productService.getProductInfo(id))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }

    @Test
    fun updateProductPrice() {
        val id = 1

        Mockito
                .`when`(priceService.updateProductPrice(id))
                .thenReturn(Function { ServerResponse.ok().build() })

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = Mono.just(UpdateProductRequest(CurrentPrice(newValue, newCurrencyCode)))

        val response = productService.updateProductPrice(id)
        assertEquals(response.apply(updateProductRequest).block()?.statusCode(), HttpStatus.OK)
    }
}