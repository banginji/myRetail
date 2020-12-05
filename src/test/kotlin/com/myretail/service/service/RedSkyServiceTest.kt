package com.myretail.service.service

import com.myretail.service.domain.ProductError
import com.myretail.service.domain.redsky.RedSkyProduct
import com.myretail.service.domain.redsky.RedSkyProductItem
import com.myretail.service.domain.redsky.RedSkyProductItemDesc
import com.myretail.service.domain.redsky.RedSkyResponse
import com.myretail.service.service.RedSkyService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.spyk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RedSkyServiceTest {
    private lateinit var redSkyService: RedSkyService

    @BeforeEach
    fun beforeEach() {
        redSkyService = spyk(RedSkyService("some host"))
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getProductTitle when data is present in redsky`() = runBlocking {
        val id = 8

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        coEvery { redSkyService.invokeRedSkyCall(id) } returns redSkyResponse

        val actualResponse = redSkyService.getProductTitle(id)

        coVerify { redSkyService.invokeRedSkyCall(id) }

        assertEquals(redSkyResponse, actualResponse)
    }

    @ExperimentalCoroutinesApi
    @Test
    fun `getProductTitle when data is not present in redsky`() = runBlocking {
        val id = 8

        val redSkyErrorResponse = RedSkyResponse(null, ProductError("could not retrieve title from redsky"));

        coEvery { redSkyService.invokeRedSkyCall(id) } throws Throwable()

        val actualResponse = redSkyService.getProductTitle(id)

        coVerify { redSkyService.invokeRedSkyCall(id) }

        assertEquals(redSkyErrorResponse, actualResponse)
    }
}