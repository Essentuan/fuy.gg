package com.busted_moments.client.framework.config.annotations

import com.busted_moments.client.framework.config.Storage

/**
 * Skips this class when generating the key for [Storage]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Skip()