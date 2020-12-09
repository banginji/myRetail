package com.myretail.service.service

import com.myretail.service.converter.PriceResponseConverter
import com.myretail.service.converter.RedSkyResponseConverter
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.converter.UpdateResponseConverter
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.PriceResponse
import com.myretail.service.domain.price.UpdatePriceRequest
import com.myretail.service.domain.product.ProductCurrentPrice
import com.myretail.service.domain.product.ProductPrice
import com.myretail.service.domain.product.UpdateProductRequest
import com.myretail.service.domain.product.UpdateProductResponse
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
    @MockK
    private lateinit var updateResponseConverter: UpdateResponseConverter

    @InjectMockKs
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun updateProductPrice() = runBlocking {
        val id = 1

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = UpdateProductRequest(ProductCurrentPrice(newValue, newCurrencyCode))
        val updatePriceRequest = UpdatePriceRequest(CurrentPrice(newValue, newCurrencyCode))
        val updateProductResponse = UpdateProductResponse(price = ProductPrice(currentPrice = ProductCurrentPrice(value = newValue, currency_code = newCurrencyCode), error = null))

        val priceResponse = PriceResponse(price = null, error = null)
        coEvery { priceService.updateProductPrice(id, updatePriceRequest) } returns priceResponse

        every { updateRequestConverter.convert(updateProductRequest) } returns updatePriceRequest

        every { updateResponseConverter.convert(priceResponse) } returns updateProductResponse

        val actualResponse = productService.updateProductPrice(id, updateProductRequest)

        assertEquals(updateProductResponse, actualResponse)
    }
}