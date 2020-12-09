package com.myretail.service.converter

import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.product.ProductName
import com.myretail.service.domain.redsky.RedSkyProduct
import com.myretail.service.domain.redsky.RedSkyProductItem
import com.myretail.service.domain.redsky.RedSkyProductItemDesc
import com.myretail.service.domain.redsky.RedSkyResponse
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RedSkyResponseConverterTest {
    private lateinit var redSkyResponseConverter: RedSkyResponseConverter

    @BeforeEach
    fun setUp() {
        redSkyResponseConverter = spyk()
    }

    @Test
    fun `maps the fields from red sky response to product name`() {
        val id = 8

        val title = "item1"
        val errorMessage = "error message"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), redSkyError = ProductError(errorMessage))

        val expectedResponse = ProductName(name = title, error = errorMessage)

        val actualResponse = redSkyResponseConverter.convert(redSkyResponse)

        assertEquals(expectedResponse, actualResponse)
    }
}