package com.myretail.service.converter

import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.persistence.PriceDocument
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UpdatePriceDocumentConverterTest {
    private lateinit var updatePriceDocumentConverter: UpdatePriceDocumentConverter

    @BeforeEach
    fun setUp() {
        updatePriceDocumentConverter = spyk()
    }

    @Test
    fun `maps the fields from a tuple of a price document and current price to a price document`() {
        val id = 8
        val value = 15.3
        val currencyCode = "EUR"

        val newValue = 20.9
        val newCurrencyCode = "USD"

        val inPriceDocument = PriceDocument(id = id, value = value, currency_code = currencyCode)
        val currentPrice = CurrentPrice(value = newValue, currency_code = newCurrencyCode)

        val expectedResponse = PriceDocument(id = id, value = newValue, currency_code = newCurrencyCode)

        val actualResponse = updatePriceDocumentConverter.convert(inPriceDocument to currentPrice)

        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `maps only new fields in current price from the price document and current price tuple to a price document`() {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"

        val newValue = 20.9

        val inPriceDocument = PriceDocument(id = id, value = value, currency_code = currencyCode)
        val currentPrice = CurrentPrice(value = newValue, currency_code = null)

        val expectedResponse = PriceDocument(id = id, value = newValue, currency_code = currencyCode)

        val actualResponse = updatePriceDocumentConverter.convert(inPriceDocument to currentPrice)

        assertEquals(expectedResponse, actualResponse)
    }
}