package com.myretail.service.persistence

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class ProductPrice(@Id val id: Int, val value: Double?, val currency_code: String?)

enum class CurrencyCode {
    USD, EUR
}