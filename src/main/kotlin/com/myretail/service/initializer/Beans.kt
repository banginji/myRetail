package com.myretail.service.initializer

import com.expediagroup.graphql.SchemaGeneratorConfig
import com.expediagroup.graphql.TopLevelObject
import com.expediagroup.graphql.toSchema
import com.myretail.service.graphql.ProductMutation
import com.myretail.service.graphql.ProductQuery
import com.myretail.service.persistence.PriceDocument
import com.myretail.service.repository.PriceRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.support.beans
import reactor.core.publisher.Flux

fun loadData() = beans {
    bean {
        CommandLineRunner {
            val productPrices = Flux.just(
                    PriceDocument(123, 14.23, "USD"),
                    PriceDocument(234, 74.24, "USD"),
                    PriceDocument(345, 593.53, "EUR"),
                    PriceDocument(456, 53.55, "USD"),
                    PriceDocument(13860428, 1193.33, "USD"),
                    PriceDocument(567, 93.33, "EUR")
            )

            ref<PriceRepository>().deleteAll()
                    .thenMany(productPrices.flatMap(ref<PriceRepository>()::save))
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