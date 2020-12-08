package com.myretail.service.repository

import com.myretail.service.persistence.PriceDocument
import org.springframework.data.mongodb.repository.ReactiveMongoRepository

interface PriceRepository: ReactiveMongoRepository<PriceDocument, Int>