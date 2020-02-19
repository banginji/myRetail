package com.myretail.service.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.myretail.service.domain.price.CurrentPrice

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductResponse(val id: Int?, val name: String?, val current_price: CurrentPrice?, val productErrors: List<Any> = emptyList())