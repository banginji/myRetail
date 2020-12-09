package com.myretail.service.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class PriceDocument(@Id val id: Int, val value: Double, val currency_code: String)
