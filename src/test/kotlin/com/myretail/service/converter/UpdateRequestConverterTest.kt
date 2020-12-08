package com.myretail.service.converter

import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.domain.product.ProductCurrentPrice
import com.myretail.service.domain.product.UpdateProductRequest
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateRequestConverterTest {
    private lateinit var updateRequestConverter: UpdateRequestConverter

    @BeforeEach
    fun setUp() {
        updateRequestConverter = spyk()
    }

    @Test
    fun `maps update product request to update price request`() {
        val value = 15.3
        val currencyCode = "USD"

        val updateProductRequest = UpdateProductRequest(currentPrice = ProductCurrentPrice(value = value, currency_code = currencyCode))

        val expectedResponse = UpdatePriceRequest(currentPrice = CurrentPrice(value = value, currency_code = currencyCode))

        val actualResponse = updateRequestConverter.convert(updateProductRequest)

        assertEquals(expectedResponse, actualResponse)
    }
}