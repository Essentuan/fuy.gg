package com.busted_moments.client.framework.config.entries.array

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class FloatList(
    kotlin: KProperty<MutableList<Float?>?>,
    annotation: Array
) : Array.Entry<Float>(kotlin, annotation) {
    private val min = annotation.floatMin
    private val max = annotation.floatMax

    override fun MutableList<Float?>.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<MutableList<Float?>, *, *> {
        return builder.startFloatList(title, this).setMin(min).setMax(max)
    }

}