package com.myretail.service.config

import com.myretail.service.handler.ProductHandler
import org.springframework.context.support.beans
import org.springframework.web.reactive.function.server.router

fun beans() = beans {
    bean { routes(ref()) }
}

fun routes(productHandler: ProductHandler) = router {
    GET("/product/{id}", productHandler::retrieveProductInfo)
}