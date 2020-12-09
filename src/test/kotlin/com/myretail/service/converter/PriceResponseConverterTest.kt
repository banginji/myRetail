package com.myretail.service.converter

import com.myretail.service.domain.price.Price
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.product.ProductCurrentPrice
import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.product.ProductPrice
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PriceResponseConverterTest {
    private lateinit var priceResponseConverter: PriceResponseConverter

    @BeforeEach
    fun setUp() {
        priceResponseConverter = spyk()
    }

    @Test
    fun `maps fields from price response to product price`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val errorMessage = "error message"

        val priceResponse = PriceResponse(price = Price(id = id, value = value, currencyCode = currencyCode), error = ProductError(errorMessage))

        val expectedResponse = ProductPrice(currentPrice = ProductCurrentPrice(value = value, currencyCode = currencyCode), error = errorMessage)

        val actualResponse = priceResponseConverter.convert(priceResponse)

        assertEquals(expectedResponse, actualResponse)
    }
}