package com.busted_moments.client.framework.config.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class Override(val value: String)