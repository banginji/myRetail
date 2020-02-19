package com.myretail.service.repository

import com.myretail.service.persistence.ProductPriceDocument
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ProductPriceRepository: ReactiveCrudRepository<ProductPriceDocument, Int>