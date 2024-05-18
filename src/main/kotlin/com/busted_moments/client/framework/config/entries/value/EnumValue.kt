package com.busted_moments.client.framework.config.entries.value

import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import net.essentuan.esl.reflections.extensions.javaClass
import kotlin.reflect.KProperty

class EnumValue<T: Enum<*>>(
    kotlin: KProperty<T?>,
    annotation: Value
) : Value.Entry<T>(kotlin, annotation) {
    @Suppress("UNCHECKED_CAST")
    override fun T.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<T, *, *> {
        return builder.startEnumSelector(
            title,
            kotlin.returnType.javaClass as Class<T>,
            this
        )
    }
}