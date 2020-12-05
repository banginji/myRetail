package com.myretail.service.initializer

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.myretail.service.graphql.ProductMutation
import com.myretail.service.graphql.ProductQuery
import com.myretail.service.persistence.ProductPriceDocument
import com.myretail.service.repository.ProductPriceRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.support.beans
import reactor.core.publisher.Flux

fun loadData() = beans {
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
}

fun graphQlBeans() = beans {
    bean {
        toSchema(
                config = SchemaGeneratorConfig(supportedPackages = listOf("com.myretail.service.domain")),
                queries = listOf(TopLevelObject(ProductQuery(ref()))),
                mutations = listOf(TopLevelObject(ProductMutation(ref())))
        )
    }
}