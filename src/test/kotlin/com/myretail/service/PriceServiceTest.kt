package com.myretail.service

import com.myretail.service.domain.CurrentPrice
import com.myretail.service.domain.ProductPriceError
import com.myretail.service.domain.ProductPriceRequest
import com.myretail.service.domain.ProductPriceResponse
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import com.myretail.service.service.PriceService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class PriceServiceTest {
    private lateinit var productPriceRepository: ProductPriceRepository

    private lateinit var priceService: PriceService

    @BeforeEach
    fun beforeEach() {
        productPriceRepository = Mockito.mock(ProductPriceRepository::class.java)
        priceService = Mockito.spy(PriceService(productPriceRepository))
    }

    @Test
    fun getProductInfo() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))
        Mockito.doReturn(Mono.just(productPriceResponse)).`when`(priceService).getProductPrice(id)

        val actualProductPriceResponse = priceService.getProductPrice(id).block()

        val actualProductPrice = actualProductPriceResponse?.productPrice
        assertEquals(actualProductPrice?.id, id)
        assertEquals(actualProductPrice?.value, value)
        assertEquals(actualProductPrice?.currency_code, currencyCode)
    }

    @Test
    fun updateProductPrice() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceRequest = ProductPriceRequest(CurrentPrice(value, currencyCode))

        Mockito.doReturn(ok().build()).`when`(priceService).updateExistingProductPrice(id, productPriceRequest)

        val response = priceService.updateProductPrice(id)
        assertEquals(response.apply(productPriceRequest).block()?.statusCode(), HttpStatus.OK)
    }

    @Test
    fun getProductPrice_whenDataIsPresentInDataStore() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.just(ProductPrice(id, value, currencyCode)))

        val productProductResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        StepVerifier
                .create(priceService.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun getProductPrice_whenDataIsNotPresentInDataStore() {
        val id = 1

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty())

        val productProductResponse = ProductPriceResponse(null, ProductPriceError("price not found in data store"))

        StepVerifier
                .create(priceService.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun updateExistingProductPrice_forAnExistingProduct() {
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
                .create(priceService.updateExistingProductPrice(id, updateProductPriceRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun updateExistingProductPrice_forANonExistingProduct() {
        val id = 1

        Mockito
                .`when`(productPriceRepository.findById(id))
                .thenReturn(Mono.empty<ProductPrice>())

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = ProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        StepVerifier
                .create(priceService.updateExistingProductPrice(id, updateProductPriceRequest))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }
}