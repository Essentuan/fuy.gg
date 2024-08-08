package com.busted_moments.client.framework.config.entries.dropdown

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.NoCopy
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.text.Text
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder
import me.shedaniel.clothconfig2.impl.builders.AbstractFieldBuilder
import me.shedaniel.clothconfig2.impl.builders.DropdownMenuBuilder
import net.essentuan.esl.iteration.Iterators
import net.essentuan.esl.model.field.Property
import net.essentuan.esl.other.unsupported
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.reflections.extensions.typeInformationOf
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Dropdown(
    val value: String,
    val provider: KClass<out Provider<*>> = Provider::class
) {
    class Entry<T : Any>(
        kotlin: KProperty<T?>,
        annotation: Dropdown
    ) : Config.Entry<T>(kotlin, annotation.value) {
        private val provider: Provider<T>

        init {
            @Suppress("UNCHECKED_CAST")
            provider =
                if (annotation.provider.isAbstract || annotation.provider.instance == null)
                    Provider(type.cls) as Provider<T>
                else
                    annotation.provider.instance as Provider<T>
        }

        override fun T.open(builder: ConfigEntryBuilder): AbstractFieldBuilder<T, *, *> =
            unsupported()

        @Suppress("UNCHECKED_CAST")
        override fun open(model: Storage, builder: ConfigEntryBuilder): AbstractConfigListEntry<*> {
            val maxWidth = provider.asSequence()
                .map { TextRenderer.split(Text(provider.name(it))).width }
                .maxOrNull() ?: 300f

            return builder.startDropdownMenu(
                title,
                DropdownMenuBuilder.TopCellElementBuilder.of(
                    (get(model) as T),
                    provider::get,
                    provider::name
                ),
                DropdownMenuBuilder.CellCreatorBuilder.ofWidth(maxWidth.toInt(), provider::name)
            ).apply {
                setSelections(provider)

                setSaveConsumer {
                    if (it == null) {
                        if (default is NoCopy)
                            set(model, mutate(default(default as T) as T))
                    } else set(model, mutate(it))
                }

                if (default !is NoCopy)
                    setDefaultValue(default(default as T) as T)

                setTooltip(*tooltip ?: return@apply)
            }.build()
        }
    }

    interface Provider<T : Any> : Iterable<T> {
        operator fun get(key: String): T?

        fun name(entry: T): Component =
            Component.literal(entry.toString())

        companion object {
            private val providers: Map<Class<*>, Provider<*>> = Reflections.types
                .subtypesOf(Provider::class)
                .map { it.instance }
                .filterNotNull()
                .associateBy {
                    it::class.java.typeInformationOf(Provider::class)["T"]!!
                }

            @Suppress("UNCHECKED_CAST")
            operator fun <T : Any> invoke(cls: Class<T>): Provider<T> {
                return (providers[cls] as Provider<T>?) ?: object : Provider<T> {
                    override fun get(key: String): T? =
                        null

                    override fun iterator(): Iterator<T> =
                        Iterators.empty()
                }
            }
        }
    }

    companion object : Config.Extension {
        override fun register(property: KProperty<Any?>): Property? {
            return Entry(property, property.tags[Dropdown::class] ?: return null)
        }
    }
}
