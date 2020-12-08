package com.myretail.service.converter

import com.myretail.service.domain.price.Price
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.product.ProductCurrentPrice
import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.product.ProductPrice
import com.myretail.service.domain.product.UpdateProductResponse
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdateResponseConverterTest {
    private lateinit var updateResponseConverter: UpdateResponseConverter

    @BeforeEach
    fun setUp() {
        updateResponseConverter = spyk()
    }

    @Test
    fun `maps the correct fields from price response to update product response`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val errorMessage = "error message"

        val priceResponse = PriceResponse(price = Price(id = id, value = value, currency_code = currencyCode), productPriceError = ProductError(errorMessage))

        val expectedResponse = UpdateProductResponse(price = ProductPrice(currentPrice = ProductCurrentPrice(value = value, currency_code = currencyCode), error = errorMessage))

        val actualResponse = updateResponseConverter.convert(priceResponse)

        assertEquals(expectedResponse, actualResponse)
    }
}