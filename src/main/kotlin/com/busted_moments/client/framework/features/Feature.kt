package com.busted_moments.client.framework.features

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.annotations.Override
import com.busted_moments.client.framework.config.entries.HiddenEntry
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.events
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.tasks
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass

abstract class Feature : Storage() {
    val name: String = Config.keyOf(this::class.simpleString())

    @Value("Enabled")
    var enabled: Boolean = this::class.tags[Default::class]?.value == State.ENABLED
        set(value) {
            if (field == value)
                return

            if (value) {
                events.register()
                tasks.resume()
            } else {
                events.unregister()
                tasks.suspend()
            }

            field = value
        }

    init {
        lateinit var enabled: Config.Entry<*>.Bound
        var count = 0

        properties.asSequence()
            .filterIsInstance<Config.Entry<*>.Bound>()
            .forEach {
                if (it.instanceOf<HiddenEntry<*>>())
                    return@forEach

                if (it.kotlin.name == "enabled")
                    enabled = it

                count++
            }

        if (count == 1) {
            enabled.title = Component.literal(this::class.tags[Override::class]?.value ?: name)
            enabled.section = null
        }
    }
}

annotation class Default(val value: State)

annotation class Overlays(vararg val value: KClass<*>)

enum class State {
    ENABLED,
    DISABLED
}