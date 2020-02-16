package com.myretail.service.domain

sealed class WrapperError()

data class ProductPriceError(val productPriceError: String?): WrapperError()

data class RedSkyError(val redSkyError: String?): WrapperError()

data class ProductError(val error: String?)