package com.busted_moments.client.framework.config.entries

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.text.Text
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import net.essentuan.esl.model.Extension
import net.essentuan.esl.model.Property
import net.essentuan.esl.optional.Optional
import net.essentuan.esl.optional.extensions.opt
import net.essentuan.esl.other.unsupported
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.tags
import net.minecraft.network.chat.Component
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

class HiddenEntry<T>(kotlin: KProperty<T?>, title: Component) : Config.Entry<T>(kotlin, title) {
    override fun T.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<T, *, *> = unsupported()

    companion object : Extension<Storage> {
        override fun register(field: KProperty<Any?>): Optional<Property> {
            return if (field is KMutableProperty<Any?>)
                field.tags[Persistent::class]?.run {
                    HiddenEntry(field, Text.component(value)).opt()
                } ?: Optional.empty()
            else
                Optional.empty()
        }
    }
}