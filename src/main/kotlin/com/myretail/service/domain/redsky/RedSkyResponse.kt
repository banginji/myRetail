package com.myretail.service.domain.redsky

import com.fasterxml.jackson.annotation.JsonInclude
import com.myretail.service.domain.product.ProductError

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RedSkyResponse(val product: RedSkyProduct?, val redSkyError: ProductError?)