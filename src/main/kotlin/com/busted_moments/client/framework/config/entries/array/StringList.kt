package com.busted_moments.client.framework.config.entries.array

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class StringList(
    kotlin: KProperty<MutableList<String?>?>,
    annotation: Array
) : Array.Entry<String>(kotlin, annotation) {
    override fun MutableList<String?>.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<MutableList<String?>, *, *> {
        return builder.startStrList(title, this)
    }
}