package com.myretail.service.converter

import com.myretail.service.domain.price.Price
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.persistence.PriceDocument
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PriceDocumentResponseConverterTest {
    private lateinit var priceDocumentResponseConverter: PriceDocumentResponseConverter

    @BeforeEach
    fun setUp() {
        priceDocumentResponseConverter = spyk()
    }

    @Test
    fun `maps fields in price document to price response`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"

        val priceDocument = PriceDocument(id = id, value = value, currency_code = currencyCode)

        val expectedResponse = PriceResponse(price = Price(id = id, value = value, currencyCode = currencyCode))

        val actualResponse = priceDocumentResponseConverter.convert(priceDocument)

        assertEquals(expectedResponse, actualResponse)
    }
}