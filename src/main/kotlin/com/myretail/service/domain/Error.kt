package com.myretail.service.domain

sealed class Error()

data class ProductError(val productError: String?): Error()

data class RedSkyError(val redSkyError: String?): Error()