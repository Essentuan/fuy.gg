package com.busted_moments.client.framework.config.entries.array

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
import net.essentuan.esl.reflections.extensions.classOf
import net.essentuan.esl.reflections.extensions.extends
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instanceof
import net.essentuan.esl.reflections.extensions.javaClass
import net.essentuan.esl.reflections.extensions.notinstanceof
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.reflections.extensions.typeArgs
import net.minecraft.network.chat.Component
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaType

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Array(
    val value: String,
    val intMin: Int = Int.MIN_VALUE,
    val intMax: Int = Int.MAX_VALUE,
    val longMin: Long = Long.MIN_VALUE,
    val longMax: Long = Long.MAX_VALUE,
    val floatMin: Float = Float.MIN_VALUE,
    val floatMax: Float = Float.MAX_VALUE,
    val doubleMin: Double = Double.MIN_VALUE,
    val doubleMax: Double = Double.MAX_VALUE,
) {
    abstract class Entry<T : Any>(
        kotlin: KProperty<MutableList<T?>?>,
        annotation: Array
    ) : Config.Entry<MutableList<T?>>(kotlin, annotation.value)

    companion object : Config.Extension {
        @Suppress("UNCHECKED_CAST")
        override fun register(field: KProperty<Any?>): Property? {
            return field.tags[Array::class]?.run {
                if (!(field.returnType.javaClass extends List::class))
                    return null

                val type = field.returnType.javaType.typeArgs().getOrNull(0)?.classOf() ?: return null

                when {
                    type extends String::class ->
                        StringList(field as KProperty<MutableList<String?>?>, this)

                    type extends Integer::class ->
                        IntList(field as KProperty<MutableList<Int?>?>, this)

                    type extends java.lang.Long::class ->
                        LongList(field as KProperty<MutableList<Long?>?>, this)

                    type extends java.lang.Float::class ->
                        FloatList(field as KProperty<MutableList<Float?>?>, this)

                    type extends java.lang.Double::class ->
                        DoubleList(field as KProperty<MutableList<Double?>?>, this)

                    else -> null
                }
            }
        }
    }
}