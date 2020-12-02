package com.myretail.service.config

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.myretail.service.graphql.ProductQuery
import com.myretail.service.handler.ProductHandler
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

fun beans() = beans {
//    bean { routes(ref()) }
    bean {
        CommandLineRunner {
            val productPrices = Flux.just(
                    ProductPriceDocument(123, 14.23, "USD"),
                    ProductPriceDocument(234, 74.24, "USD"),
                    ProductPriceDocument(345, 593.53, "EUR"),
                    ProductPriceDocument(456, 53.55, "USD"),
                    ProductPriceDocument(13860428, 1193.33, "USD"),
                    ProductPriceDocument(567, 93.33, "EUR")
            )

            ref<ProductPriceRepository>().deleteAll()
                    .thenMany(productPrices.flatMap(ref<ProductPriceRepository>()::save))
                    .subscribe(::println)
        }
    }
    bean {
        toSchema(
                config = SchemaGeneratorConfig(supportedPackages = listOf("com.myretail.service.domain")),
                queries = listOf(TopLevelObject(ProductQuery(ref())))
        )
    }
}

//fun routes(productHandler: ProductHandler) = router {
//    "/product/{id}".nest {
//        GET("", productHandler::getProductInfo)
//        PUT("", productHandler::updateProductPrice)
//    }
//}