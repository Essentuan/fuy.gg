package com.busted_moments.client.framework.artemis.config

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.entries.HiddenEntry
import com.busted_moments.client.framework.config.entries.value.ColorValue
import com.wynntils.core.consumers.features.AbstractConfigurable
import com.wynntils.core.persisted.PersistedOwner
import com.wynntils.core.persisted.type.PersistedMetadata
import net.essentuan.esl.model.Model
import java.lang.reflect.Type
import kotlin.reflect.jvm.javaField

typealias Store<T> = com.wynntils.core.persisted.config.Config<T>

@Suppress("UNCHECKED_CAST")
open class LinkedConfig<T>(
    val model: Storage,
    val entry: Config.Entry<T>,
    owner: PersistedOwner,
    type: Type = entry.element.javaField!!.genericType,
    default: T = entry.default as T
) : Store<T>(entry[model] as T) {
    val metadata = PersistedMetadata(
        owner,
        entry.element.name,
        type,
        default,
        "",
        entry.type.isNullable,
        entry.key
    )

    override fun touched() = Config.write()

    override fun get(): T =
        entry[model] as T

    override fun store(value: T) {
        entry[model] = value
    }

    override fun getDisplayName(): String = entry.title.string

    override fun getDescription(): String {
        return entry.tooltip?.joinToString('\n'.toString()) { it.string } ?: "No Description"
    }
}

fun Storage.linkTo(configurable: AbstractConfigurable) {
    configurable.addConfigOptions(
        Model.descriptor(javaClass)
            .asSequence()
            .filterNot { it !is Config.Entry<*> || it is HiddenEntry<*> }
            .map {
                when (it) {
                    is ColorValue -> ColorConfig(this, it, configurable)
                    else -> @Suppress("UNCHECKED_CAST") LinkedConfig(this, it as Config.Entry<Any?>, configurable)
                }
            }.toList()
    )
}

fun <T> T.link() where T: Storage, T: AbstractConfigurable = linkTo(this)