package com.busted_moments.client.framework.config.entries.sliders

import com.busted_moments.client.framework.config.entries.value.Value
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class LongSlider(
    kotlin: KProperty<Long?>,
    annotation: Slider
) : Slider.Entry<Long>(kotlin, annotation) {
    private val min = annotation.longMin
    private val max = annotation.longMax

    override fun Long.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<Long, *, *> {
        return builder.startLongSlider(
            title,
            this,
            min,
            max
        )
    }
}