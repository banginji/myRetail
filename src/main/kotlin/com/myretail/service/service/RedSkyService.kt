package com.myretail.service.service

import com.myretail.service.domain.product.ProductError
import com.myretail.service.domain.redsky.RedSkyResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlinx.coroutines.flow.single
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

@Service
class RedSkyService(@Value("\${redsky.host:https://redsky.target.com}") private val host: String) {
    private val webClient: WebClient = WebClient.create(host)
    private val logger = KotlinLogging.logger {  }

    suspend fun getProductTitle(id: Int) = flow { emit(invokeRedSkyCall(id)) }
            .retry(3) { e -> (e is Exception).also { if (it) delay(50) } }
            .catch { emit(redSkyError()) }
            .single()

    suspend fun invokeRedSkyCall(id: Int): RedSkyResponse {
        logger.info { "Invoking red sky call" }
        return webClient.get().uri(rsUri(id)).retrieve().awaitBody<RedSkyResponse>()
    }

    private fun rsUri(id: Int) = "/v3/pdp/tcin/$id?excludes=taxonomy,price,promotion,bulk_ship,rating_and_review_reviews,rating_and_review_statistics,question_answer_statistics&key=candidate"

    private fun redSkyError() = RedSkyResponse(null, ProductError("could not retrieve title from redsky"))
}