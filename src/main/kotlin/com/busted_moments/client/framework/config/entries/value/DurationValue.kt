package com.busted_moments.client.framework.config.entries.value

import com.busted_moments.client.framework.config.NoCopy
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.text.Text
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import net.essentuan.esl.other.unsupported
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.FormatFlag
import java.util.Optional
import java.util.function.Consumer
import kotlin.reflect.KProperty

class DurationValue(
    kotlin: KProperty<Duration?>,
    annotation: Value
) : Value.Entry<Duration>(kotlin, annotation) {
    @Suppress("UNCHECKED_CAST")
    override fun Duration.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<Duration, *, *> =
        unsupported()

    @Suppress("UNCHECKED_CAST")
    override fun open(model: Storage, builder: ConfigEntryBuilder): AbstractConfigListEntry<*> {
        val value = get(model) as Duration

        return builder
            .startStrField(title, value.print(FormatFlag.COMPACT))
            .apply {
                saveConsumer = Consumer { set(model, Duration(it)) }

                setErrorSupplier {
                    if (Duration(it) == null)
                        Optional.of(
                            Text.component("Invalid duration!")
                        )
                    else
                        Optional.empty()
                }

                if (default !is NoCopy)
                    setDefaultValue((default(default as Duration) as Duration).print(FormatFlag.COMPACT))

                setTooltip(*tooltip ?: return@apply)
            }.build()

//        (get(model) as Duration).open(builder).apply {
//            saveConsumer = Consumer { set(model, mutate(it)) }
//            @Suppress("UNCHECKED_CAST")
//
//            if (default !is NoCopy)
//                setDefaultValue(default(default as T) as T)
//
//            setTooltip(*tooltip ?: return@apply)
//        }.build()
    }
}