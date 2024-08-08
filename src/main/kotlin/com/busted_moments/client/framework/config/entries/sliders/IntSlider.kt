package com.busted_moments.client.framework.config.entries.sliders

import com.busted_moments.client.framework.config.entries.value.Value
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class IntSlider(
    kotlin: KProperty<Int?>,
    annotation: Slider
) : Slider.Entry<Int>(kotlin, annotation) {
    private val min = annotation.intMin
    private val max = annotation.intMax

    override fun Int.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<Int, *, *> {
        return builder.startIntSlider(
            title,
            this,
            min,
            max
        )
    }
}