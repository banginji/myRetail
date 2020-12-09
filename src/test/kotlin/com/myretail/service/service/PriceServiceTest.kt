package com.myretail.service.service

import com.myretail.service.converter.PriceDocumentResponseConverter
import com.myretail.service.converter.UpdatePriceDocumentConverter
import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.Price
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.persistence.PriceDocument
import com.myretail.service.repository.PriceRepository
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
    private lateinit var priceRepository: PriceRepository
    @MockK
    private lateinit var priceDocumentResponseConverter: PriceDocumentResponseConverter
    @MockK
    private lateinit var updatePriceDocumentConverter: UpdatePriceDocumentConverter

    @InjectMockKs
    private lateinit var priceService: PriceService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `getProductPrice when data is present in data store`() = runBlocking {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceDocument = PriceDocument(id, value, currencyCode)
        val productProductResponse = PriceResponse(Price(id, value, currencyCode))

        every { priceRepository.findById(id) } returns Mono.just(productPriceDocument)
        every { priceDocumentResponseConverter.convert(productPriceDocument) } returns productProductResponse

        val actualResponse = priceService.getProductPrice(id)

        verify { priceRepository.findById(id) }
        verify { priceDocumentResponseConverter.convert(productPriceDocument) }

        assertEquals(productProductResponse, actualResponse)
    }

    @Test
    fun `getProductPrice when data is not present in data store`() = runBlocking {
        val id = 1

        val priceErrorResponse = PriceResponse(null, ProductError("price not found in data store"))

        every { priceRepository.findById(id) } returns Mono.empty()

        val actualResponse = priceService.getProductPrice(id)

        verify(exactly = 1) { priceRepository.findById(id) }
        verify { priceDocumentResponseConverter.convert(any()) wasNot called }

        assertEquals(priceErrorResponse, actualResponse)
    }

    @Test
    fun `updateExistingProductPrice for an existing product`() = runBlocking {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPrice = PriceDocument(id, value, currencyCode)

        every { priceRepository.findById(id) } returns Mono.just(productPrice)

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = UpdatePriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val updatedProductPrice = PriceDocument(id, newValue, newCurrencyCode)
        val updatedProductProductResponse = PriceResponse(Price(id, newValue, newCurrencyCode))

        every { updatePriceDocumentConverter.convert(productPrice to updateProductPriceRequest.currentPrice) } returns updatedProductPrice
        every { priceRepository.save(updatedProductPrice) } returns Mono.just(updatedProductPrice)
        every { priceDocumentResponseConverter.convert(updatedProductPrice) } returns updatedProductProductResponse

        val actualResponse = priceService.updateProductPrice(id, updateProductPriceRequest)

        verify(exactly = 1) { priceRepository.findById(id) }
        verify(exactly = 1) { priceRepository.save(updatedProductPrice) }
        verify(exactly = 1) { updatePriceDocumentConverter.convert(productPrice to updateProductPriceRequest.currentPrice) }
        verify(exactly = 1) { priceDocumentResponseConverter.convert(updatedProductPrice) }

        assertEquals(updatedProductProductResponse, actualResponse)
    }

    @Test
    fun `updateExistingProductPrice for a non existing product`() = runBlocking {
        val id = 1

        val priceErrorResponse = PriceResponse(null, ProductError("price not found in data store"))

        every { priceRepository.findById(id) } returns Mono.empty()

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductPriceRequest = UpdatePriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val actualResponse = priceService.updateProductPrice(id, updateProductPriceRequest)

        verify(exactly = 1) { priceRepository.findById(id) }
        verify { priceRepository.save(any()) wasNot called }
        verify { updatePriceDocumentConverter.convert(any()) wasNot called }
        verify { priceDocumentResponseConverter.convert(any()) wasNot called }

        assertEquals(priceErrorResponse, actualResponse)
    }
}