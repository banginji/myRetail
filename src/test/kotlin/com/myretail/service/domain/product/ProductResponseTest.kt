package com.myretail.service.domain.product

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.redsky.RedSkyResponse
import com.myretail.service.service.PriceService
import com.myretail.service.service.RedSkyService
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProductResponseTest {
    private lateinit var priceService: PriceService
    private lateinit var redSkyService: RedSkyService
    private lateinit var redSkyResponseConverter: RedSkyResponseConverter
    private lateinit var priceResponseConverter: PriceResponseConverter

    private lateinit var productResponse: ProductResponse

    private val id: Int = 0

    @BeforeEach
    fun setUp() {
        priceService = mockk()
        redSkyService = mockk()
        redSkyResponseConverter = mockk()
        priceResponseConverter = mockk()
        productResponse = spyk(ProductResponse(id, priceService, redSkyService, redSkyResponseConverter, priceResponseConverter))
    }

    @Test
    fun `invoking name field makes the right calls to converters and services`(): Unit = runBlocking {
        val redSkyResponse = RedSkyResponse(product = null, redSkyError = null)
        coEvery { redSkyService.getProductTitle(id) } returns redSkyResponse

        val productName = ProductName(name = null, error = null)
        every { redSkyResponseConverter.convert(redSkyResponse) } returns productName

        val actualResponse = productResponse.name()

        coVerify(exactly = 1) { redSkyService.getProductTitle(id) }
        verify(exactly = 1) { redSkyResponseConverter.convert(redSkyResponse) }

        assertEquals(productName, actualResponse)
    }

    @Test
    fun `invoking price field makes the right calls to converters and services`(): Unit = runBlocking {
        val priceResponse = PriceResponse(price = null)
        coEvery { priceService.getProductPrice(id) } returns priceResponse

        val productPrice = ProductPrice(currentPrice = null, error = null)
        every { priceResponseConverter.convert(priceResponse) } returns productPrice

        val actualResponse = productResponse.price()

        coVerify(exactly = 1) { priceService.getProductPrice(id) }
        verify(exactly = 1) { priceResponseConverter.convert(priceResponse) }

        assertEquals(productPrice, actualResponse)
    }
}