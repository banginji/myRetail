package com.myretail.service

import com.myretail.service.initializer.graphQlBeans
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.support.GenericApplicationContext

class BeansInitializer : ApplicationContextInitializer<GenericApplicationContext> {
    override fun initialize(ctx: GenericApplicationContext) = graphQlBeans().initialize(ctx)
}