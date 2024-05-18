package com.busted_moments.client.framework.config.entries.value

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class BooleanValue(
    kotlin: KProperty<Boolean?>,
    annotation: Value
) : Value.Entry<Boolean>(kotlin, annotation) {
    override fun Boolean.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<Boolean, *, *> =
        create(builder::startBooleanToggle)
}