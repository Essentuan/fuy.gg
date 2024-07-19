package com.busted_moments.client.framework.config.entries.sliders

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.entries.value.BooleanValue
import com.busted_moments.client.framework.config.entries.value.ColorValue
import com.busted_moments.client.framework.config.entries.value.EnumValue
import com.busted_moments.client.framework.config.entries.value.NumberValue
import com.busted_moments.client.framework.config.entries.value.StringValue
import com.busted_moments.client.framework.config.entries.value.Value
import com.google.common.primitives.Primitives
import net.essentuan.esl.color.Color
import net.essentuan.esl.model.field.Property
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instanceof
import net.essentuan.esl.reflections.extensions.javaClass
import net.essentuan.esl.reflections.extensions.tags
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Slider(
    val value: String,
    val intMin: Int = Int.MIN_VALUE,
    val intMax: Int = Int.MAX_VALUE,
    val longMin: Long = Long.MIN_VALUE,
    val longMax: Long = Long.MAX_VALUE
) {
    abstract class Entry<T>(
        kotlin: KProperty<T?>,
        annotation: Slider
    ) : Config.Entry<T>(kotlin, annotation.value)

    companion object : Config.Extension {
        @Suppress("UNCHECKED_CAST")
        override fun register(field: KProperty<Any?>): Property? {
            return field.tags[Slider::class]?.run {
                val type = Primitives.wrap(field.returnType.javaClass)

                when {
                    type instanceof Integer::class ->
                        IntSlider(field as KProperty<Int?>, this)

                    type instanceof java.lang.Long::class ->
                        LongSlider(field as KProperty<Long?>, this)

                    else -> null
                }
            }
        }
    }
}
