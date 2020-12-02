package com.myretail.service

import com.myretail.service.domain.price.*
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import com.myretail.service.service.PriceService
import io.mockk.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.ServerResponse.ok
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class PriceServiceTest {
    private lateinit var productPriceRepository: ProductPriceRepository

    private lateinit var priceService: PriceService

    @BeforeEach
    fun beforeEach() {
        productPriceRepository = mockkClass(ProductPriceRepository::class)
        priceService = spyk(PriceService(productPriceRepository))
    }

    @Test
    fun getProductInfo() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))
        every { priceService.getProductPrice(id) } returns Mono.just(productPriceResponse)

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

        val productPriceRequest = UpdateProductPriceRequest(CurrentPrice(value, currencyCode))

        every { priceService.updateExistingProductPrice(id, productPriceRequest) } returns ok().build()

        val response = priceService.updateProductPrice(id)
        assertEquals(response.apply(productPriceRequest).block()?.statusCode(), HttpStatus.OK)
    }

    @Test
    fun `getProductPrice when data is present in data store`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        every { productPriceRepository.findById(id) } returns Mono.just(ProductPriceDocument(id, value, currencyCode))

        val productProductResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        StepVerifier
                .create(priceService.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun `getProductPrice when data is not present in data store`() {
        val id = 1

        every { productPriceRepository.findById(id) } returns Mono.empty()

        val productProductResponse = ProductPriceResponse(null, ProductPriceError("price not found in data store"))

        StepVerifier
                .create(priceService.getProductPrice(id))
                .expectNext(productProductResponse)
                .verifyComplete()
    }

    @Test
    fun `updateExistingProductPrice for an existing product`() {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPrice = ProductPriceDocument(id, value, currencyCode)

        every { productPriceRepository.findById(id) } returns Mono.just(productPrice)

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = UpdateProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val updatedProductPrice = ProductPriceDocument(id, newValue, newCurrencyCode)

        every { productPriceRepository.save(updatedProductPrice) } returns Mono.just(updatedProductPrice)

        StepVerifier
                .create(priceService.updateExistingProductPrice(id, updateProductPriceRequest))
                .expectNextMatches { it.statusCode().is2xxSuccessful }
                .verifyComplete()
    }

    @Test
    fun `updateExistingProductPrice for a non existing product`() {
        val id = 1

        every { productPriceRepository.findById(id) } returns Mono.empty()

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = UpdateProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        StepVerifier
                .create(priceService.updateExistingProductPrice(id, updateProductPriceRequest))
                .expectNextMatches { it.statusCode().is4xxClientError }
                .verifyComplete()
    }
}