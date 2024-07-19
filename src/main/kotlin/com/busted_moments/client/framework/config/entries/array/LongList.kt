package com.busted_moments.client.framework.config.entries.array

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class LongList(
    kotlin: KProperty<MutableList<Long?>?>,
    annotation: Array
) : Array.Entry<Long>(kotlin, annotation) {
    private val min = annotation.longMin
    private val max = annotation.longMax

    override fun MutableList<Long?>.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<MutableList<Long?>, *, *> {
        return builder.startLongList(title, this).setMin(min).setMax(max)
    }
}