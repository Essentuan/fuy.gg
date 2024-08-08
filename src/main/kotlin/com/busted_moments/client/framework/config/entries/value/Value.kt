package com.busted_moments.client.framework.config.entries.value

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.Storage
import com.google.common.primitives.Primitives
import net.essentuan.esl.color.Color
import net.essentuan.esl.model.Extension
import net.essentuan.esl.model.field.Property
import net.essentuan.esl.reflections.extensions.extends
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instanceof
import net.essentuan.esl.reflections.extensions.javaClass
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.time.duration.Duration
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty


@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Value(
    val value: String,
    val intMin: Int = Int.MIN_VALUE,
    val intMax: Int = Int.MAX_VALUE,
    val longMin: Long = Long.MIN_VALUE,
    val longMax: Long = Long.MAX_VALUE,
    val floatMin: Float = Float.MIN_VALUE,
    val floatMax: Float = Float.MAX_VALUE,
    val doubleMin: Double = Double.MIN_VALUE,
    val doubleMax: Double = Double.MAX_VALUE,
    val alpha: Boolean = false
) {
    abstract class Entry<T>(
        kotlin: KProperty<T?>,
        annotation: Value
    ) : Config.Entry<T>(kotlin, annotation.value)

    companion object : Config.Extension {
        @Suppress("UNCHECKED_CAST")
        override fun register(field: KProperty<Any?>): Property? {
            return field.tags[Value::class]?.run {
                val type = Primitives.wrap(field.returnType.javaClass)

                when {
                    type extends java.lang.Boolean::class ->
                        BooleanValue(field as KProperty<Boolean?>, this)

                    type extends Color::class ->
                        ColorValue(field as KProperty<Color?>, this)

                    type extends Enum::class ->
                        EnumValue(field as KProperty<Enum<*>?>, this)

                    type extends String::class ->
                        StringValue(field as KProperty<String?>, this)

                    type extends Integer::class ->
                        NumberValue.Int(field as KProperty<Int?>, this)

                    type extends java.lang.Long::class ->
                        NumberValue.Long(field as KProperty<Long?>, this)

                    type extends java.lang.Float::class ->
                        NumberValue.Float(field as KProperty<Float?>, this)

                    type extends java.lang.Double::class ->
                        NumberValue.Double(field as KProperty<Double?>, this)

                    type extends Duration::class ->
                        DurationValue(field as KProperty<Duration?>, this)

                    else -> null
                }
            }
        }
    }
}