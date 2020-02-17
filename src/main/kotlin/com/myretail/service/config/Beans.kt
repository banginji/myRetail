package com.myretail.service.config

import com.myretail.service.handler.ProductHandler
import com.myretail.service.persistence.ProductPrice
import com.myretail.service.repository.ProductPriceRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Flux

fun beans() = beans {
    bean { routes(ref()) }
    bean {
        CommandLineRunner {
            val productPrices = Flux.just(
                    ProductPrice(123, 14.23, "USD"),
                    ProductPrice(234, 74.24, "USD"),
                    ProductPrice(345, 593.53, "EUR"),
                    ProductPrice(456, 53.55, "USD"),
                    ProductPrice(13860428, 1193.33, "USD"),
                    ProductPrice(567, 93.33, "EUR")
            )

            ref<ProductPriceRepository>().deleteAll()
                    .thenMany(productPrices.flatMap(ref<ProductPriceRepository>()::save))
                    .subscribe(::println)
        }
    }
}

fun routes(productHandler: ProductHandler) = router {
    "/product/{id}".nest {
        GET("", productHandler::getProductInfo)
        PUT("", productHandler::updateProductPrice)
    }
}