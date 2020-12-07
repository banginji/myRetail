package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.domain.ProductError
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPrice
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.price.UpdateProductPriceRequest
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import io.mockk.MockKAnnotations
import io.mockk.called
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class PriceServiceTest {
    @MockK
    private lateinit var productPriceRepository: ProductPriceRepository
    @MockK
    private lateinit var priceResponseConverter: PriceResponseConverter

    @InjectMockKs
    private lateinit var priceService: PriceService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `getProductPrice when data is present in data store`() = runBlocking {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceDocument = ProductPriceDocument(id, value, currencyCode)
        val productProductResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        every { productPriceRepository.findById(id) } returns Mono.just(productPriceDocument)
        every { priceResponseConverter.convert(productPriceDocument) } returns productProductResponse

        val actualResponse = priceService.getProductPrice(id)

        verify { productPriceRepository.findById(id) }
        verify { priceResponseConverter.convert(productPriceDocument) }

        assertEquals(productProductResponse, actualResponse)
    }

    @Test
    fun `getProductPrice when data is not present in data store`() = runBlocking {
        val id = 1

        val priceErrorResponse = ProductPriceResponse(null, ProductError("price not found in data store"))

        every { productPriceRepository.findById(id) } returns Mono.empty()

        val actualResponse = priceService.getProductPrice(id)

        verify(exactly = 1) { productPriceRepository.findById(id) }
        verify { priceResponseConverter.convert(any()) wasNot called }

        assertEquals(priceErrorResponse, actualResponse)
    }

    @Test
    fun `updateExistingProductPrice for an existing product`() = runBlocking {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPrice = ProductPriceDocument(id, value, currencyCode)

        every { productPriceRepository.findById(id) } returns Mono.just(productPrice)

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = UpdateProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val updatedProductPrice = ProductPriceDocument(id, newValue, newCurrencyCode)
        val updatedProductProductResponse = ProductPriceResponse(ProductPrice(id, newValue, newCurrencyCode))

        every { productPriceRepository.save(updatedProductPrice) } returns Mono.just(updatedProductPrice)
        every { priceResponseConverter.convert(updatedProductPrice) } returns updatedProductProductResponse

        val actualResponse = priceService.updateProductPrice(id, updateProductPriceRequest)

        verify(exactly = 1) { productPriceRepository.findById(id) }
        verify(exactly = 1) { productPriceRepository.save(updatedProductPrice) }
        verify(exactly = 1) { priceResponseConverter.convert(updatedProductPrice) }

        assertEquals(updatedProductProductResponse, actualResponse)
    }

    @Test
    fun `updateExistingProductPrice for a non existing product`() = runBlocking {
        val id = 1

        val priceErrorResponse = ProductPriceResponse(null, ProductError("price not found in data store"))

        every { productPriceRepository.findById(id) } returns Mono.empty()

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = UpdateProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val actualResponse = priceService.updateProductPrice(id, updateProductPriceRequest)

        verify(exactly = 1) { productPriceRepository.findById(id) }
        verify { productPriceRepository.save(any()) wasNot called }
        verify { priceResponseConverter.convert(any()) wasNot called }

        assertEquals(priceErrorResponse, actualResponse)
    }
}