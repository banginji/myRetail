package com.myretail.service.service

import com.myretail.service.converter.ProductResponseConverter
import com.myretail.service.converter.UpdateRequestConverter
import com.myretail.service.domain.ProductError
import com.myretail.service.domain.ProductResponse
import com.myretail.service.domain.UpdateProductRequest
import com.myretail.service.domain.price.CurrentPrice
import com.myretail.service.domain.price.ProductPrice
import com.myretail.service.domain.price.ProductPriceResponse
import com.myretail.service.domain.price.UpdateProductPriceRequest
import com.myretail.service.domain.redsky.RedSkyProduct
import com.myretail.service.domain.redsky.RedSkyProductItem
import com.myretail.service.domain.redsky.RedSkyProductItemDesc
import com.myretail.service.domain.redsky.RedSkyResponse
import io.mockk.*
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
    private lateinit var productResponseConverter: ProductResponseConverter
    @MockK
    private lateinit var updateRequestConverter: UpdateRequestConverter

    @InjectMockKs
    private lateinit var productService: ProductService

    @BeforeEach
    fun setUp() = MockKAnnotations.init(this)

    @Test
    fun `getProductInfo for successful aggregation of data across multiple sources`() = runBlocking {
        val id = 1
        val value = 1.1
        val currencyCode = "USD"

        val productPriceResponse = ProductPriceResponse(ProductPrice(id, value, currencyCode))

        coEvery { priceService.getProductPrice(id) } returns productPriceResponse

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        coEvery { redSkyService.getProductTitle(id) } returns redSkyResponse

        val expectedResponse = ProductResponse(id = id, name = title, current_price = CurrentPrice(value = value, currency_code = currencyCode))
        every { productResponseConverter.convert(productPriceResponse to redSkyResponse) } returns expectedResponse

        val actualResponse = productService.getProductInfo(id)

        coVerify(exactly = 1) { priceService.getProductPrice(id) }
        coVerify(exactly = 1) { redSkyService.getProductTitle(id) }
        verify(exactly = 1) { productResponseConverter.convert(any()) }

        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `getProductInfo for successful aggregation from at least one source`() = runBlocking {
        val id = 1

        val priceError = ProductPriceResponse(null, ProductError("price not found in data store"))

        coEvery { priceService.getProductPrice(id) } returns priceError

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        coEvery { redSkyService.getProductTitle(id) } returns redSkyResponse

        val expectedResponse = ProductResponse(id = id, name = title, current_price = null)
        every { productResponseConverter.convert(priceError to redSkyResponse) } returns expectedResponse

        val actualResponse = productService.getProductInfo(id)

        coVerify(exactly = 1) { priceService.getProductPrice(id) }
        coVerify(exactly = 1) { redSkyService.getProductTitle(id) }
        verify(exactly = 1) { productResponseConverter.convert(any()) }

        assertEquals(expectedResponse, actualResponse)
    }

    @Test
    fun `getProductInfo for failure to obtain from two sources`() = runBlocking {
        val id = 1

        val priceResponse = ProductPriceResponse(null, ProductError("price not found in data store"))
        coEvery { priceService.getProductPrice(id) } returns priceResponse

        val redSkyResponse = RedSkyResponse(null, ProductError("could not retrieve title from redsky"))
        coEvery { redSkyService.getProductTitle(id) } returns redSkyResponse

        val productResponse = ProductResponse(id = null, name = null, current_price = null)
        every { productResponseConverter.convert(priceResponse to redSkyResponse) } returns productResponse

        val actualResponse = productService.getProductInfo(id)

        coVerify(exactly = 1) { priceService.getProductPrice(id) }
        coVerify(exactly = 1) { redSkyService.getProductTitle(id) }
        verify(exactly = 1) { productResponseConverter.convert(any()) }

        assertEquals(productResponse, actualResponse)
    }

    @Test
    fun updateProductPrice() = runBlocking {
        val id = 1

        val newValue = 2.2
        val newCurrencyCode = "EUR"
        val updateProductRequest = UpdateProductRequest(CurrentPrice(newValue, newCurrencyCode))
        val updateProductPriceRequest = UpdateProductPriceRequest(CurrentPrice(newValue, newCurrencyCode))

        val productPriceResponse = ProductPriceResponse(productPrice = null, productPriceError = null)
        coEvery { priceService.updateProductPrice(id, updateProductPriceRequest) } returns productPriceResponse

        every { updateRequestConverter.convert(updateProductRequest) } returns updateProductPriceRequest

        val actualResponse = productService.updateProductPrice(id, updateProductRequest)

        assertEquals(productPriceResponse, actualResponse)
    }
}