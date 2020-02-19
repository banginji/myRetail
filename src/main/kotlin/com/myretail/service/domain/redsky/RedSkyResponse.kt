package com.myretail.service.domain.redsky

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class RedSkyResponse(val product: RedSkyProduct?, val redSkyError: RedSkyError?)