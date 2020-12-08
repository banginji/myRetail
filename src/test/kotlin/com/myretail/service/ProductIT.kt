package com.myretail.service

import com.expediagroup.graphql.types.GraphQLRequest
import com.myretail.service.domain.redsky.RedSkyProduct
import com.myretail.service.domain.redsky.RedSkyProductItem
import com.myretail.service.domain.redsky.RedSkyProductItemDesc
import com.myretail.service.domain.redsky.RedSkyResponse
import com.myretail.service.persistence.PriceDocument
import com.myretail.service.repository.PriceRepository
import com.myretail.service.service.RedSkyService
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.SpykBean
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest
@ContextConfiguration(initializers = [BeansInitializer::class])
@AutoConfigureWebTestClient
class ProductIT(@Autowired private val client : WebTestClient) {
    @MockkBean
    private lateinit var priceRepository: PriceRepository

    @SpykBean
    private lateinit var redSkyService: RedSkyService

    private final val baseJsonPath = "$.data.getProductInfo"
    private final val graphQLEndpoint = "/graphql"

    @Test
    fun `get product response when data exists in data store and redsky`(): Unit = runBlocking {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val title = "item1"

        every { priceRepository.findById(id) } returns Mono.just(PriceDocument(id, value, currencyCode))

        coEvery { redSkyService.invokeRedSkyCall(id) } returns RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        /**
         * {
            "id": 13860428,
            "name": "The Big Lebowski (Blu-ray)",
            "current_price": {
                "value": 1193.33,
                "currency_code": "USD"
            },
            "productErrors": []
        }
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { current_price { value, currency_code } name id productErrors { error } } }")

        client
                .post()
                .uri(graphQLEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
                .jsonPath("$.data").exists()
                .jsonPath(baseJsonPath).exists()
                .jsonPath("$baseJsonPath.id").isEqualTo(id)
                .jsonPath("$baseJsonPath.name").isEqualTo(title)
                .jsonPath("$baseJsonPath.current_price").exists()
                .jsonPath("$baseJsonPath.current_price.value").isEqualTo(value)
                .jsonPath("$baseJsonPath.current_price.currency_code").isEqualTo(currencyCode)
                .jsonPath("$baseJsonPath.productErrors").isArray
                .jsonPath("$baseJsonPath.productErrors.length()").isEqualTo(0)

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 1) { redSkyService.invokeRedSkyCall(id) }
    }

    @Test
    fun `get product response when data exists in data store but not in redsky`(): Unit = runBlocking {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"

        val redSkyErrorMessage = "could not retrieve title from redsky"

        every { priceRepository.findById(id) } returns Mono.just(PriceDocument(id, value, currencyCode))

        coEvery { redSkyService.invokeRedSkyCall(id) } throws Exception()

        /**
         * {
            "id": 234,
            "current_price": {
                "value": 74.24,
                "currency_code": "USD"
            },
            "productErrors": [
                {
                    "redSkyError": "could not retrieve title from redsky"
                }
            ]
        }
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { current_price { value, currency_code } name id productErrors { error } } }")

        client
                .post()
                .uri(graphQLEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath(baseJsonPath).exists()
                .jsonPath("$baseJsonPath.id").isEqualTo(id)
                .jsonPath("$baseJsonPath.name").doesNotExist()
                .jsonPath("$baseJsonPath.current_price").exists()
                .jsonPath("$baseJsonPath.current_price.value").isEqualTo(value)
                .jsonPath("$baseJsonPath.current_price.currency_code").isEqualTo(currencyCode)
                .jsonPath("$baseJsonPath.productErrors").isArray
                .jsonPath("$baseJsonPath.productErrors.length()").isEqualTo(1)
                .jsonPath("$baseJsonPath.productErrors.[0].error").exists()
                .jsonPath("$baseJsonPath.productErrors.[0].error").isEqualTo(redSkyErrorMessage)

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 4) { redSkyService.invokeRedSkyCall(id) }
    }

    @Test
    fun `get product response when data not exist in data store but exists in redsky`(): Unit = runBlocking {
        val id = 8
        val title = "item1"
        val productPriceError = "price not found in data store"

        every { priceRepository.findById(id) } returns Mono.empty()

        coEvery { redSkyService.invokeRedSkyCall(id) } returns RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        /**
         * {
            "id": 13860427,
            "name": "Conan the Barbarian (dvd_video)",
            "productErrors": [
                {
                    "productPriceError": "price not found in data store"
                }
            ]
        }
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { current_price { value, currency_code } name id productErrors { error } } }")

        client
                .post()
                .uri(graphQLEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath(baseJsonPath).exists()
                .jsonPath("$baseJsonPath.id").isEqualTo(id)
                .jsonPath("$baseJsonPath.name").isEqualTo(title)
                .jsonPath("$baseJsonPath.current_price").doesNotExist()
                .jsonPath("$baseJsonPath.productErrors").isArray
                .jsonPath("$baseJsonPath.productErrors.length()").isEqualTo(1)
                .jsonPath("$baseJsonPath.productErrors.[0].error").exists()
                .jsonPath("$baseJsonPath.productErrors.[0].error").isEqualTo(productPriceError)

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 1) { redSkyService.invokeRedSkyCall(id) }
    }

    @Test
    fun `get product response when data not exist in both data store and redsky`(): Unit = runBlocking {
        val id = 8

        val redSkyErrorMessage = "could not retrieve title from redsky"
        val productPriceError = "price not found in data store"

        every { priceRepository.findById(id) } returns Mono.empty()

        coEvery { redSkyService.invokeRedSkyCall(id) } throws Exception()

        /**
         * {
            "productErrors": [
                {
                    "productPriceError": "price not found in data store"
                },
                {
                    "redSkyError": "could not retrieve title from redsky"
                }
            ]
        }
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { current_price { value, currency_code } name id productErrors { error } } }")

        client
                .post()
                .uri(graphQLEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath(baseJsonPath).exists()
                .jsonPath("$baseJsonPath.id").doesNotExist()
                .jsonPath("$baseJsonPath.name").doesNotExist()
                .jsonPath("$baseJsonPath.current_price").doesNotExist()
                .jsonPath("$baseJsonPath.productErrors").isArray
                .jsonPath("$baseJsonPath.productErrors.length()").isEqualTo(2)
                .jsonPath("$baseJsonPath.productErrors.[0].error").exists()
                .jsonPath("$baseJsonPath.productErrors.[0].error").isEqualTo(productPriceError)
                .jsonPath("$baseJsonPath.productErrors.[1].error").exists()
                .jsonPath("$baseJsonPath.productErrors.[1].error").isEqualTo(redSkyErrorMessage)

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 4) { redSkyService.invokeRedSkyCall(id) }
    }

    fun `get product response for client requested fields alone`() = runBlocking {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val title = "item1"

        every { priceRepository.findById(id) } returns Mono.just(PriceDocument(id, value, currencyCode))

        coEvery { redSkyService.invokeRedSkyCall(id) } returns RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        /**
         * {
            "id": 13860428,
            "name": "The Big Lebowski (Blu-ray)",
            "current_price": {
            "value": 1193.33,
            "currency_code": "USD"
            },
            "productErrors": []
        }
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { current_price { value } name } }")

        client
                .post()
                .uri(graphQLEndpoint)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$").exists()
                .jsonPath("$.data").exists()
                .jsonPath(baseJsonPath).exists()
                .jsonPath("$baseJsonPath.id").doesNotExist()
                .jsonPath("$baseJsonPath.name").isEqualTo(title)
                .jsonPath("$baseJsonPath.current_price").exists()
                .jsonPath("$baseJsonPath.current_price.value").isEqualTo(value)
                .jsonPath("$baseJsonPath.current_price.currency_code").doesNotExist()
                .jsonPath("$baseJsonPath.productErrors").doesNotExist()

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 1) { redSkyService.invokeRedSkyCall(id) }
    }
}
