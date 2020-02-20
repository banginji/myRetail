package com.myretail.service.service

import com.myretail.service.domain.redsky.RedSkyError
import com.myretail.service.domain.redsky.RedSkyResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration
import java.util.function.Function

@Service
class RedSkyService(@Value("\${redsky.host:https://redsky.target.com}") private val host: String) {
    private val webClient: WebClient = WebClient.create(host)

    fun getProductTitle(id: Int) = Flux
            .interval(Duration.ofMillis(200))
            .flatMap(invokeRedSkyCall(id))
            .retryBackoff(3, Duration.ofMillis(100), Duration.ofSeconds(1), 0.1)
            .take(1)
            .next()
            .onErrorResume(::redSkyError)

    fun invokeRedSkyCall(id: Int) = Function<Long, Mono<RedSkyResponse>> {
        webClient
                .get()
                .uri("/v2/pdp/tcin/$id?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics")
                .retrieve()
                .bodyToMono()
    }

    private fun redSkyError(throwable: Throwable) =
            Mono.just(RedSkyResponse(null, RedSkyError("could not retrieve title from redsky: (${throwable.message})")))
}