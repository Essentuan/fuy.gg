package com.busted_moments.client.framework.config.entries.value

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

abstract class NumberValue<T : Number>(
    kotlin: KProperty<T?>,
    annotation: Value,
    val min: T,
    val max: T
) : Value.Entry<T>(kotlin, annotation) {
    class Int(
        kotlin: KProperty<kotlin.Int?>,
        annotation: Value
    ) : NumberValue<kotlin.Int>(kotlin, annotation, annotation.intMin, annotation.intMax) {
        override fun kotlin.Int.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<kotlin.Int, *, *> =
            create(builder::startIntField).setMin(min).setMax(max)
    }

    class Long(
        kotlin: KProperty<kotlin.Long?>,
        annotation: Value
    ) : NumberValue<kotlin.Long>(kotlin, annotation, annotation.longMin, annotation.longMax) {
        override fun kotlin.Long.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<kotlin.Long, *, *> =
            create(builder::startLongField).setMin(min).setMax(max)
    }

    class Float(
        kotlin: KProperty<kotlin.Float?>,
        annotation: Value
    ) : NumberValue<kotlin.Float>(kotlin, annotation, annotation.floatMin, annotation.floatMax) {
        override fun kotlin.Float.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<kotlin.Float, *, *> =
            create(builder::startFloatField).setMin(min).setMax(max)
    }

    class Double(
        kotlin: KProperty<kotlin.Double?>,
        annotation: Value
    ) : NumberValue<kotlin.Double>(kotlin, annotation, annotation.doubleMin, annotation.doubleMax) {
        override fun kotlin.Double.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<kotlin.Double, *, *> =
            create(builder::startDoubleField).setMin(min).setMax(max)
    }
}