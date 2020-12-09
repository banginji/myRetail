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
import io.mockk.*
import io.mockk.coVerify
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

    private final val baseGetJsonPath = "$.data.getProductInfo"
    private final val baseUpdateJsonPath = "$.data.updateProductInfo"
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
                .jsonPath(baseGetJsonPath).exists()
                .jsonPath("$baseGetJsonPath.id").isEqualTo(id)
                .jsonPath("$baseGetJsonPath.name").exists()
                .jsonPath("$baseGetJsonPath.name.name").isEqualTo(title)
                .jsonPath("$baseGetJsonPath.name.error").doesNotExist()
                .jsonPath("$baseGetJsonPath.price").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice.value").isEqualTo(value)
                .jsonPath("$baseGetJsonPath.price.currentPrice.currencyCode").isEqualTo(currencyCode)
                .jsonPath("$baseGetJsonPath.price.error").doesNotExist()

        verify(exactly = 1) { priceRepository.findById(id) }
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
                .jsonPath(baseGetJsonPath).exists()
                .jsonPath("$baseGetJsonPath.id").isEqualTo(id)
                .jsonPath("$baseGetJsonPath.price").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice.value").isEqualTo(value)
                .jsonPath("$baseGetJsonPath.price.currentPrice.currencyCode").isEqualTo(currencyCode)
                .jsonPath("$baseGetJsonPath.name").exists()
                .jsonPath("$baseGetJsonPath.name.name").doesNotExist()
                .jsonPath("$baseGetJsonPath.name.error").isEqualTo(redSkyErrorMessage)

        verify(exactly = 1) { priceRepository.findById(id) }
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
                .jsonPath(baseGetJsonPath).exists()
                .jsonPath("$baseGetJsonPath.id").isEqualTo(id)
                .jsonPath("$baseGetJsonPath.price").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice").doesNotExist()
                .jsonPath("$baseGetJsonPath.price.error").isEqualTo(productPriceError)
                .jsonPath("$baseGetJsonPath.name").exists()
                .jsonPath("$baseGetJsonPath.name.name").isEqualTo(title)
                .jsonPath("$baseGetJsonPath.name.error").doesNotExist()

        verify(exactly = 1) { priceRepository.findById(id) }
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
                .jsonPath(baseGetJsonPath).exists()
                .jsonPath("$baseGetJsonPath.id").isEqualTo(id)
                .jsonPath("$baseGetJsonPath.price").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice").doesNotExist()
                .jsonPath("$baseGetJsonPath.price.error").isEqualTo(productPriceError)
                .jsonPath("$baseGetJsonPath.name").exists()
                .jsonPath("$baseGetJsonPath.name.name").doesNotExist()
                .jsonPath("$baseGetJsonPath.name.error").isEqualTo(redSkyErrorMessage)

        verify(exactly = 1) { priceRepository.findById(id) }
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
                .jsonPath(baseGetJsonPath).exists()
                .jsonPath("$baseGetJsonPath.id").doesNotExist()
                .jsonPath("$baseGetJsonPath.name").exists()
                .jsonPath("$baseGetJsonPath.name.name").isEqualTo(title)
                .jsonPath("$baseGetJsonPath.name.error").doesNotExist()
                .jsonPath("$baseGetJsonPath.price").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice.value").isEqualTo(value)
                .jsonPath("$baseGetJsonPath.price.currentPrice.currencyCode").doesNotExist()
                .jsonPath("$baseGetJsonPath.price.error").doesNotExist()

        verify(exactly = 1) { priceRepository.findById(id) }
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
                .jsonPath(baseGetJsonPath).exists()
                .jsonPath("$baseGetJsonPath.name").doesNotExist()
                .jsonPath("$baseGetJsonPath.price").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice").exists()
                .jsonPath("$baseGetJsonPath.price.currentPrice.value").isEqualTo(value)

        verify(exactly = 1) { priceRepository.findById(id) }
        coVerify { redSkyService.invokeRedSkyCall(id) wasNot called }
    }

    @Test
    fun `update product response when data exists in data store`(): Unit = runBlocking {
        val id = 8
        val value = 15.3
        val currencyCode = "EUR"

        val newValue = 20.9
        val newCurrencyCode = "USD"

        val originalPriceDocument = PriceDocument(id, value, currencyCode)
        val newPriceDocument = PriceDocument(id, newValue, newCurrencyCode)

        every { priceRepository.findById(id) } returns Mono.just(originalPriceDocument)
        every { priceRepository.save(newPriceDocument) } returns Mono.just(newPriceDocument)

        /**
         *
         * {
            "data": {
                "updateProductInfo": {
                    "price": {
                        "currentPrice": {
                            "value": 20.9,
                            "currencyCode": "USD"
                        },
                        "error": null
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "mutation { updateProductInfo(id: $id, updateProductRequest: { newPrice: { value: $newValue, currencyCode: \"$newCurrencyCode\" }}) { price { currentPrice { value, currencyCode } error } } }")

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
            .jsonPath(baseUpdateJsonPath).exists()
            .jsonPath("$baseUpdateJsonPath.price").exists()
            .jsonPath("$baseUpdateJsonPath.price.currentPrice").exists()
            .jsonPath("$baseUpdateJsonPath.price.currentPrice.value").isEqualTo(newValue)
            .jsonPath("$baseUpdateJsonPath.price.currentPrice.currencyCode").isEqualTo(newCurrencyCode)
            .jsonPath("$baseUpdateJsonPath.price.error").doesNotExist()

        verify(exactly = 1) { priceRepository.findById(id) }
        verify(exactly = 1) { priceRepository.save(newPriceDocument) }
    }

    @Test
    fun `update product response when data does not exists in data store`(): Unit = runBlocking {
        val id = 8

        val newValue = 20.9
        val newCurrencyCode = "USD"

        val errorMessage = "price not found in data store"

        every { priceRepository.findById(id) } returns Mono.empty()
        every { priceRepository.save(any()) } returns Mono.empty()

        /**
         *
         * {
            "data": {
                "updateProductInfo": {
                    "price": {
                        "currentPrice": null,
                        "error": "price not found in data store"
                    }
                }
            }
        }
         *
         */

        val request = GraphQLRequest(query = "mutation { updateProductInfo(id: $id, updateProductRequest: { newPrice: { value: $newValue, currencyCode: \"$newCurrencyCode\" }}) { price { currentPrice { value, currencyCode } error } } }")

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
            .jsonPath(baseUpdateJsonPath).exists()
            .jsonPath("$baseUpdateJsonPath.price").exists()
            .jsonPath("$baseUpdateJsonPath.price.currentPrice").doesNotExist()
            .jsonPath("$baseUpdateJsonPath.price.error").isEqualTo(errorMessage)

        verify(exactly = 1) { priceRepository.findById(id) }
        verify { priceRepository.save(any()) wasNot called }
    }
}
