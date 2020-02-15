package com.myretail.service.repository

import com.myretail.service.persistence.ProductPrice
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface ProductPriceRepository: ReactiveCrudRepository<ProductPrice, Int>