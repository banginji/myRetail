package com.myretail.service

import com.myretail.service.domain.*
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import com.myretail.service.service.ProductService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.util.function.Function

class ProductServiceTest {
    private lateinit var productPriceRepository: ProductPriceRepository

    private lateinit var productService: ProductService

    @BeforeEach
    fun beforeEach() {
        productPriceRepository = Mockito.mock(ProductPriceRepository::class.java)
        productService = Mockito.spy(ProductService(productPriceRepository))
    }

    @Test
    fun `getProductInfo`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))
        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(productService).getProductPrice(id)

        val title = "item1"

        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)
        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(productService).getProductTitle(id)

        val response = productService.getProductInfo(id)

        val actualProductPriceResponse = response.first.block()
        val actualProductPrice = actualProductPriceResponse?.productPrice
        assertEquals(actualProductPrice?.id, id)
        assertEquals(actualProductPrice?.value, value)
        assertEquals(actualProductPrice?.currency_code, currencyCode)

        val actualRedSkyResponse = response.second.block()
        assertEquals(actualRedSkyResponse?.product?.item?.tcin, id.toString())
        assertEquals(actualRedSkyResponse?.product?.item?.product_description?.title, title)
    }

    @Test
    fun `updateProductPrice`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceRequest = ProductPriceRequest(CurrentPrice(value, currencyCode))

        Mockito.doReturn(ok().build()).`when`(productService).updateExistingProductPrice(id, productPriceRequest)

        val response = productService.updateProductPrice(id)
        assertEquals(response.apply(productPriceRequest).block()?.statusCode(), HttpStatus.OK)
    }

    @Test
    fun `getProductPrice_whenDataIsPresentInDataStore`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(ProductPrice(id, value, currencyCode)))

        val productProductResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        StepVerifier
                .create(productService.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun `getProductPrice_whenDataIsNotPresentInDataStore`() {
        val id = 1

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty())

        val productProductResponse = ProductPriceResponse(null, ProductPriceError("price not found in data store"))

        StepVerifier
                .create(productService.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun `getProductTitle_whenDataIsPresentInRedsky`() {
        val id = 8

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.just(redSkyResponse) })
                .`when`(productService).invokeRedSkyCall(id)

        StepVerifier
                .withVirtualTime { productService.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(redSkyResponse)
                .verifyComplete()
    }

    @Test
    fun `getProductTitle_whenDataIsNotPresentInRedsky`() {
        val id = 8

        val redSkyErrorMessage = "could not retrieve title from redsky: (Retries exhausted: 3/3)"

        Mockito.doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) })
                .`when`(productService).invokeRedSkyCall(id)

        StepVerifier
                .withVirtualTime { productService.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(RedSkyResponse(null, RedSkyError(redSkyErrorMessage)))
                .verifyComplete()
    }

    @Test
    fun `updateExistingProductPrice_forAnExistingProduct`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPrice = ProductPrice(id, value, currencyCode)

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(productPrice))

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = ProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val updatedProductPrice = ProductPrice(id, newValue, newCurrencyCode)
        Mockito
                .`when`(productPriceRepository.save(updatedProductPrice))
                .thenReturn(Mono.just(updatedProductPrice))

        StepVerifier
                .create(productService.updateExistingProductPrice(id, updateProductPriceRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `updateExistingProductPrice_forANonExistingProduct`() {
        val id = 1

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty<ProductPrice>())

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = ProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        StepVerifier
                .create(productService.updateExistingProductPrice(id, updateProductPriceRequest))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }
}