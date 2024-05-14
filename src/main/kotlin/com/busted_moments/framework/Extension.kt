package com.busted_moments.framework

import com.wynntils.core.WynntilsMod
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.tags

interface Extension {
    val sounds: Array<out String>
        get() = this::class.tags[Sounds::class]?.value ?: emptyArray()

    val scan: Array<out String>
        get() = this::class.tags[Scan::class]?.value ?: emptyArray()

    /**
     * Called once [WynntilsMod.init] has finished initialization.
     */
    fun init() = Unit
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Scan(vararg val value: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Sounds(vararg val value: String)