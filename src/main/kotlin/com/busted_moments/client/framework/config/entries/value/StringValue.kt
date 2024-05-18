package com.busted_moments.client.framework.config.entries.value

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import kotlin.reflect.KProperty

class StringValue(
    kotlin: KProperty<String?>,
    annotation: Value
) : Value.Entry<String>(kotlin, annotation) {
    override fun String.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<String, *, *> =
        create(builder::startStrField)
}