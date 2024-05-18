package com.busted_moments.client.framework.config.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS, AnnotationTarget.PROPERTY)
annotation class Section(val value: String)
