package com.myretail.service

import com.myretail.service.config.beans
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class MyRetailApplication

fun main(args: Array<String>) {
    runApplication<MyRetailApplication>(*args) { addInitializers(beans()) }
}
