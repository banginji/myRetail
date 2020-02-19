package com.myretail.service

import com.myretail.service.domain.redsky.*
import com.myretail.service.service.RedSkyService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.util.function.Function

class RedSkyServiceTest {
    private lateinit var redSkyService: RedSkyService

    @BeforeEach
    fun beforeEach() {
        redSkyService = Mockito.spy(RedSkyService())
    }

    @Test
    fun getProductTitle() {
        val id = 1
        val title = "item1"

        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)
        Mockito.doReturn(Mono.just(redSkyResponse)).`when`(redSkyService).getProductTitle(id)

        val actualRedSkyResponse = redSkyService.getProductTitle(id).block()
        assertEquals(actualRedSkyResponse?.product?.item?.tcin, id.toString())
        assertEquals(actualRedSkyResponse?.product?.item?.product_description?.title, title)
    }

    @Test
    fun getProductTitle_whenDataIsPresentInRedsky() {
        val id = 8

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        Mockito.doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.just(redSkyResponse) })
                .`when`(redSkyService).invokeRedSkyCall(id)

        StepVerifier
                .withVirtualTime { redSkyService.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(redSkyResponse)
                .verifyComplete()
    }

    @Test
    fun getProductTitle_whenDataIsNotPresentInRedsky() {
        val id = 8

        val redSkyErrorMessage = "could not retrieve title from redsky: (Retries exhausted: 3/3)"

        Mockito.doReturn(Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) })
                .`when`(redSkyService).invokeRedSkyCall(id)

        StepVerifier
                .withVirtualTime { redSkyService.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(RedSkyResponse(null, RedSkyError(redSkyErrorMessage)))
                .verifyComplete()
    }
}