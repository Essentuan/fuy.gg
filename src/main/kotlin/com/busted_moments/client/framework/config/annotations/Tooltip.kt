package com.busted_moments.client.framework.config.annotations

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Tooltip(val value: Array<String>)
