package com.myretail.service.handler

import com.myretail.service.domain.ErrorMsg
import com.myretail.service.domain.RedSkyResponse
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse.badRequest
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.body
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Duration

@Service
class ProductHandler {

    private val host = "https://redsky.target.com"
    private val webClient: WebClient = WebClient.create(host)

    fun retrieveProductInfo(request: ServerRequest) = Flux
            .interval(Duration.ofMillis(200))
            .flatMap {
                webClient
                        .get()
                        .uri("/v2/pdp/tcin/${request.pathVariable("id")}?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics")
                        .retrieve()
                        .onStatus(HttpStatus::is4xxClientError) { Mono.empty() }
                        .onStatus(HttpStatus::is5xxServerError) { Mono.error<Throwable>(Throwable("redsky not available")) }
                        .bodyToMono<RedSkyResponse>()
            }
            .retryBackoff(3, Duration.ofMillis(100))
            .take(1)
            .flatMap { ok().body<RedSkyResponse>(Mono.just(it)) }
            .last()
            .onErrorResume(::errorResponse)
}

private fun errorResponse(throwable: Throwable) = badRequest().body<ErrorMsg>(Mono.just(ErrorMsg(throwable.message)))
