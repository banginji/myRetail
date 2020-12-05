package com.myretail.service.repository

import com.myretail.service.persistence.ProductPriceDocument
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface ProductPriceRepository: ReactiveMongoRepository<ProductPriceDocument, Int>