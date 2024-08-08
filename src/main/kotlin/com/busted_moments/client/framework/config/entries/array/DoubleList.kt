package com.busted_moments.client.framework.config.entries.array

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class DoubleList(
    kotlin: KProperty<MutableList<Double?>?>,
    annotation: Array
) : Array.Entry<Double>(kotlin, annotation) {
    private val min = annotation.doubleMin
    private val max = annotation.doubleMax

    override fun MutableList<Double?>.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<MutableList<Double?>, *, *> {
        return builder.startDoubleList(title, this).setMin(min).setMax(max)
    }
}