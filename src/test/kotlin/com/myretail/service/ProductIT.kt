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
import io.mockk.called
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
         *
         * {
            "data": {
                "getProductInfo": {
                    "id": 13860428,
                    "price": {
                        "currentPrice": {
                            "value": 1193.33,
                            "currencyCode": "USD"
                        },
                        "error": null
                    },
                    "name": {
                        "name": "The Big Lebowski (Blu-ray)",
                        "error": null
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { price { currentPrice { value, currencyCode } error } name { name error } id } }")

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
                .jsonPath("$baseJsonPath.name").exists()
                .jsonPath("$baseJsonPath.name.name").isEqualTo(title)
                .jsonPath("$baseJsonPath.name.error").doesNotExist()
                .jsonPath("$baseJsonPath.price").exists()
                .jsonPath("$baseJsonPath.price.currentPrice").exists()
                .jsonPath("$baseJsonPath.price.currentPrice.value").isEqualTo(value)
                .jsonPath("$baseJsonPath.price.currentPrice.currencyCode").isEqualTo(currencyCode)
                .jsonPath("$baseJsonPath.price.error").doesNotExist()

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
         *
         * {
            "data": {
                "getProductInfo": {
                    "id": 13860428,
                    "price": {
                        "currentPrice": {
                            "value": 1193.33,
                            "currencyCode": "USD"
                        },
                        "error": null
                    },
                    "name": {
                        "name": null,
                        "error": "could not retrieve title from redsky"
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { price { currentPrice { value, currencyCode } error } name { name error } id } }")

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
                .jsonPath("$baseJsonPath.price").exists()
                .jsonPath("$baseJsonPath.price.currentPrice").exists()
                .jsonPath("$baseJsonPath.price.currentPrice.value").isEqualTo(value)
                .jsonPath("$baseJsonPath.price.currentPrice.currencyCode").isEqualTo(currencyCode)
                .jsonPath("$baseJsonPath.name").exists()
                .jsonPath("$baseJsonPath.name.name").doesNotExist()
                .jsonPath("$baseJsonPath.name.error").isEqualTo(redSkyErrorMessage)

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
         *
         * {
            "data": {
                "getProductInfo": {
                    "id": 13860428,
                    "price": {
                        "currentPrice": null
                        "error": "price not found in data store"
                    },
                    "name": {
                        "name": "The Big Lebowski (Blu-ray)",
                        "error": null
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { price { currentPrice { value, currencyCode } error } name { name error } id } }")

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
                .jsonPath("$baseJsonPath.price").exists()
                .jsonPath("$baseJsonPath.price.currentPrice").doesNotExist()
                .jsonPath("$baseJsonPath.price.error").isEqualTo(productPriceError)
                .jsonPath("$baseJsonPath.name").exists()
                .jsonPath("$baseJsonPath.name.name").isEqualTo(title)
                .jsonPath("$baseJsonPath.name.error").doesNotExist()

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
         *
         * {
            "data": {
                "getProductInfo": {
                    "id": 13860428,
                    "price": {
                        "currentPrice": null,
                        "error": "price not found in data store"
                    },
                    "name": {
                        "name": null,
                        "error": "could not retrieve title from redsky"
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { price { currentPrice { value, currencyCode } error } name { name error } id } }")

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
                .jsonPath("$baseJsonPath.price").exists()
                .jsonPath("$baseJsonPath.price.currentPrice").doesNotExist()
                .jsonPath("$baseJsonPath.price.error").isEqualTo(productPriceError)
                .jsonPath("$baseJsonPath.name").exists()
                .jsonPath("$baseJsonPath.name.name").doesNotExist()
                .jsonPath("$baseJsonPath.name.error").isEqualTo(redSkyErrorMessage)

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 4) { redSkyService.invokeRedSkyCall(id) }
    }

    @Test
    fun `get product response for client requested fields alone`() = runBlocking {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val title = "item1"

        every { priceRepository.findById(id) } returns Mono.just(PriceDocument(id, value, currencyCode))

        coEvery { redSkyService.invokeRedSkyCall(id) } returns RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        /**
         *
         * {
            "data": {
                "getProductInfo": {
                    "id": 13860428,
                    "price": {
                        "currentPrice": {
                            "value": 1193.33
                        }
                    },
                    "name": {
                        "name": "The Big Lebowski (Blu-ray)"
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { price { currentPrice { value } } name { name } } }")

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
                .jsonPath("$baseJsonPath.name").exists()
                .jsonPath("$baseJsonPath.name.name").isEqualTo(title)
                .jsonPath("$baseJsonPath.name.error").doesNotExist()
                .jsonPath("$baseJsonPath.price").exists()
                .jsonPath("$baseJsonPath.price.currentPrice").exists()
                .jsonPath("$baseJsonPath.price.currentPrice.value").isEqualTo(value)
                .jsonPath("$baseJsonPath.price.currentPrice.currencyCode").doesNotExist()
                .jsonPath("$baseJsonPath.price.error").doesNotExist()

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify(exactly = 1) { redSkyService.invokeRedSkyCall(id) }
    }

    @Test
    fun `get product response does not make a call to redsky when name is not requested by client`() = runBlocking {
        val id = 8
        val value = 15.3
        val currencyCode = "USD"
        val title = "item1"

        every { priceRepository.findById(id) } returns Mono.just(PriceDocument(id, value, currencyCode))

        coEvery { redSkyService.invokeRedSkyCall(id) } returns RedSkyResponse(RedSkyProduct(RedSkyProductItem(id.toString(), RedSkyProductItemDesc(title))), null)

        /**
         *
         * {
            "data": {
                "getProductInfo": {
                    "id": 13860428,
                    "price": {
                        "currentPrice": {
                            "value": 1193.33
                        }
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "{ getProductInfo(id: 8) { price { currentPrice { value } } } }")

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
                .jsonPath("$baseJsonPath.name").doesNotExist()
                .jsonPath("$baseJsonPath.price").exists()
                .jsonPath("$baseJsonPath.price.currentPrice").exists()
                .jsonPath("$baseJsonPath.price.currentPrice.value").isEqualTo(value)

        coVerify(exactly = 1) { priceRepository.findById(id) }
        coVerify { redSkyService.invokeRedSkyCall(id) wasNot called }
    }
}
