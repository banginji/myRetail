package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.domain.product.UpdateProductRequest
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProductServiceTest {
    @MockK
    private lateinit var priceService: PriceService
    @MockK
    private lateinit var redSkyService: RedSkyService
    @MockK
    private lateinit var priceResponseConverter: PriceResponseConverter
    @MockK
    private lateinit var redSkyResponseConverter: RedSkyResponseConverter
    @MockK
    private lateinit var updateRequestConverter: UpdateRequestConverter

    @InjectMockKs
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun updateProductPrice() = runBlocking {
        val id = 1

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = UpdateProductRequest(CurrentPrice(newValue, newCurrencyCode))
        val updatePriceRequest = UpdatePriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val productPriceResponse = PriceResponse(price = null, productPriceError = null)
        coEvery { priceService.updateProductPrice(id, updatePriceRequest) } returns productPriceResponse

        every { updateRequestConverter.convert(updateProductRequest) } returns updatePriceRequest

        val actualResponse = productService.updateProductPrice(id, updateProductRequest)

        assertEquals(productPriceResponse, actualResponse)
    }
}