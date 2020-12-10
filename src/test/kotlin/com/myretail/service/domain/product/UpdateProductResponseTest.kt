package com.myretail.service.domain.product

import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.converter.UpdateResponseConverter
import com.myretail.service.domain.price.NewPrice
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.service.PriceService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateProductResponseTest {
    private val id = 1
    private val newValue = 2.2
    private val newCurrencyCode = "EUR"
    private val updateProductRequest: UpdateProductRequest = UpdateProductRequest(ProductNewPrice(newValue, newCurrencyCode))

    private lateinit var priceService: PriceService
    private lateinit var updateRequestConverter: UpdateRequestConverter
    private lateinit var updateResponseConverter: UpdateResponseConverter

    private lateinit var updateProductResponse: UpdateProductResponse

    @BeforeEach
    fun setUp() {
        priceService = mockk()
        updateRequestConverter = mockk()
        updateResponseConverter = mockk()

        updateProductResponse = spyk(UpdateProductResponse(id, updateProductRequest, priceService, updateRequestConverter, updateResponseConverter))
    }

    @Test
    fun `makes the right calls to the service and converters to populate the price field`() = runBlocking {
        val updatePriceRequest = UpdatePriceRequest(NewPrice(newValue, newCurrencyCode))
        val productPrice = ProductPrice(currentPrice = ProductCurrentPrice(value = newValue, currencyCode = newCurrencyCode), error = null)

        val priceResponse = PriceResponse(price = null, error = null)
        coEvery { priceService.updateProductPrice(id, updatePriceRequest) } returns priceResponse

        every { updateRequestConverter.convert(updateProductRequest) } returns updatePriceRequest

        every { updateResponseConverter.convert(priceResponse) } returns productPrice

        val actualResponse = updateProductResponse.price()

        Assertions.assertEquals(productPrice, actualResponse)
    }
}