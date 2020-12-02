package com.myretail.service

import com.myretail.service.domain.redsky.*
import com.myretail.service.service.RedSkyService
import io.mockk.every
import io.mockk.spyk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Duration
import java.util.function.Function

class RedSkyServiceTest {
    private lateinit var redSkyService: RedSkyService

    @BeforeEach
    fun beforeEach() {
        redSkyService = spyk(RedSkyService("some host"))
    }

    @Test
    fun `getProductTitle when data is present in redsky`() {
        val id = 8

        val title = "item1"
        val redSkyResponse = RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        every { redSkyService.invokeRedSkyCall(id) } returns Function<Long, Mono<RedSkyResponse>> { Mono.just(redSkyResponse) }

        StepVerifier
                .withVirtualTime { redSkyService.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(redSkyResponse)
                .verifyComplete()
    }

    @Test
    fun `getProductTitle when data is not present in redsky`() {
        val id = 8

        val redSkyErrorMessage = "could not retrieve title from redsky: (Retries exhausted: 3/3)"

        every { redSkyService.invokeRedSkyCall(id) } returns Function<Long, Mono<RedSkyResponse>> { Mono.error(Throwable()) }

        StepVerifier
                .withVirtualTime { redSkyService.getProductTitle(id) }
                .thenAwait(Duration.ofMinutes(1))
                .expectNext(RedSkyResponse(null, RedSkyError(redSkyErrorMessage)))
                .verifyComplete()
    }
}