package com.busted_moments.client.framework.config.entries.array

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class IntList(
    kotlin: KProperty<MutableList<Int?>?>,
    annotation: Array
) : Array.Entry<Int>(kotlin, annotation) {
    private val min = annotation.intMin
    private val max = annotation.intMax

    override fun MutableList<Int?>.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<MutableList<Int?>, *, *> {
        return builder.startIntList(title, this).setMin(min).setMax(max)
    }
}