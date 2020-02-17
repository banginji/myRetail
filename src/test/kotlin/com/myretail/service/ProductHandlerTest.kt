package com.myretail.service

import com.myretail.service.domain.*
import com.myretail.service.handler.ProductHandler
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.util.function.Function

class ProductHandlerTest {
    private lateinit var productPriceRepository: ProductPriceRepository

    private lateinit var productHandler: ProductHandler

    @BeforeEach
    fun beforeEach() {
        productPriceRepository = Mockito.mock(ProductPriceRepository::class.java)
        productHandler = Mockito.spy(ProductHandler(productPriceRepository))
    }

    @Test
    fun `get product info for successful aggregation of data across multiple sources`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))
        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(productHandler).getProductPrice(id)

        val title = "item1"

        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)
        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(productHandler).getProductTitle(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .assertNext { it.statusCode() == HttpStatus.OK }
                .verifyComplete()
    }

    @Test
    fun `get product info for successful aggregation from at least one source`() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))
        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(productHandler).getProductPrice(id)

        val title = "item1"

        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)
        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(productHandler).getProductTitle(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .assertNext { it.statusCode() == HttpStatus.OK }
                .verifyComplete()
    }

    @Test
    fun `get product info for failure to obtain from two sources`() {
        val id = 1

        val productPriceResponse = ProductPriceResponse(null, ProductPriceError("product error"))
        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(productHandler).getProductPrice(id)

        val redSkyResponse = RedSkyResponse(null, RedSkyError("redsky error"))
        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(productHandler).getProductTitle(id)

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).build()

        StepVerifier
                .create(productHandler.getProductInfo(serverRequest))
                .assertNext { it.statusCode() == HttpStatus.NOT_FOUND }
                .verifyComplete()
    }

    @Test
    fun `get product price when data is present in data store`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(ProductPrice(id, value, currencyCode)))

        val productProductResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        StepVerifier
                .create(productHandler.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun `get product price when data is not present in data store`() {
        val id = 1

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty())

        val productProductResponse = ProductPriceResponse(null, ProductPriceError("price not found in data store"))

        StepVerifier
                .create(productHandler.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun `get product title when data is present in redsky`() {
        val id = 8

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.just(redSkyResponse) })
                .`when`(productHandler).invokeRedSkyCall(id)

        StepVerifier
                .withVirtualTime { productHandler.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(redSkyResponse)
                .verifyComplete()
    }

    @Test
    fun `get product title when data is not present in redsky`() {
        val id = 8

        val redSkyErrorMessage = "could not retrieve title from redsky: (Retries exhausted: 3/3)"

        Mockito.doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) })
                .`when`(productHandler).invokeRedSkyCall(id)

        StepVerifier
                .withVirtualTime { productHandler.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(RedSkyResponse(null, RedSkyError(redSkyErrorMessage)))
                .verifyComplete()
    }

    @Test
    fun `update product price for an existing product`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPrice = ProductPrice(id, value, currencyCode)

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(productPrice))

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = ProductRequest(CurrentPrice(newValue, newCurrencyCode))

        val updatedProductPrice = ProductPrice(id, newValue, newCurrencyCode)
        Mockito
                .`when`(productPriceRepository.save(updatedProductPrice))
                .thenReturn(Mono.just(updatedProductPrice))

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).body(Mono.just(updateProductRequest))

        StepVerifier
                .create(productHandler.updateProductPrice(serverRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `update product price for a non existing product`() {
        val id = 1

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty<ProductPrice>())

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = ProductRequest(CurrentPrice(newValue, newCurrencyCode))

        val serverRequest = MockServerRequest.builder().pathVariable("id", id.toString()).body(Mono.just(updateProductRequest))

        StepVerifier
                .create(productHandler.updateProductPrice(serverRequest))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }
}