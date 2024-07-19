package com.busted_moments.client.framework.config.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class Tooltip(val value: Array<String>)
