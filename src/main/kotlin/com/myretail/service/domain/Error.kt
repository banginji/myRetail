package com.myretail.service.domain

sealed class Error()

data class ProductPriceError(val productPriceError: String?): Error()

data class RedSkyError(val redSkyError: String?): Error()