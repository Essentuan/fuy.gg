package com.busted_moments.client.framework.features

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.annotations.Override
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.HiddenEntry
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.text.Text
import com.busted_moments.mixin.invoker.FeatureManagerInvoker
import com.busted_moments.mixin.invoker.OverlayManagerInvoker
import com.wynntils.core.components.Managers
import com.wynntils.core.consumers.features.Feature
import com.wynntils.core.consumers.overlays.RenderState
import com.wynntils.core.persisted.config.Category
import com.wynntils.core.persisted.config.ConfigCategory
import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent
import com.wynntils.mc.event.RenderEvent
import net.essentuan.esl.Rating
import net.essentuan.esl.encoding.StringBasedEncoder
import net.essentuan.esl.model.Model
import net.essentuan.esl.model.annotations.Ignored
import net.essentuan.esl.model.annotations.Sorted
import net.essentuan.esl.orNull
import net.essentuan.esl.other.Base64
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.tasks
import net.essentuan.esl.unsafe
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.EventPriority
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type
import kotlin.reflect.KClass

typealias WynntilsFeature = Feature

abstract class Feature : Storage {
    @Ignored
    private var delegate: Delegate? = null

    val key: String = Config.keyOf(this::class.simpleString())
    val name: String = Config.nameOf(key)

    @Value("Enabled")
    @Sorted(Rating.HIGHEST)
    var enabled: Boolean = this::class.tags[Default::class]?.value != State.DISABLED
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
            delegate?.setUserEnabled(value)
        }

    @Persistent
    private var replaced: MutableSet<String> = mutableSetOf()

    init {
        run {
            val overlays = this::class[Overlays::class] ?: return@run

            if (overlays.value.isEmpty())
                return@run

            delegate = Delegate()
            (Managers.Feature as FeatureManagerInvoker).invokeRegisterFeature(delegate)

            for (cls in overlays.value) {
                (Managers.Overlay as OverlayManagerInvoker).invokeRegisterOverlay(
                    cls.instance!!.also { it.claim(this@Feature) },
                    delegate,
                    RenderEvent.ElementType.GUI,
                    cls[RenderAt::class]?.value ?: RenderState.POST,
                    this::class.tags[Default::class]?.value != State.DISABLED
                )
            }
        }
    }

    protected open fun onEnable() = Unit

    protected open fun onDisable() = Unit

    @Subscribe
    private fun ConnectedEvent.on() {
        if (!enabled)
            return

        this@Feature::class[Replaces::class]?.let {
            val replacing = it.value
                .asSequence()
                .map { cls -> Base64.encode(cls.java.name) }
                .toMutableSet()

            if (replacing != replaced) {
                replaced = mutableSetOf()

                for (cls in it.value) {
                    Managers.Feature.getFeatureInstance(cls.java as Class<WynntilsFeature>).run {
                        setUserEnabled(false)
                        userEnabled.setValue(false)
                    }
                    replaced += Base64.encode(cls.java.name)
                }
            }
        }
    }

    override fun ready() {
        lateinit var enabled: Config.Entry<*>
        var count = 0

        for (cls in this::class[Overlays::class]?.value ?: emptyArray())
            count += Model.descriptor(cls).count { it is Config.Entry<*> && it !is HiddenEntry<*> }

        Model.descriptor(javaClass)
            .filterIsInstance<Config.Entry<*>>()
            .filterNot { it is HiddenEntry<*> }
            .forEach {
                count++

                if (it.element.name != "enabled")
                    return@forEach

                enabled = it

                enabled.tooltip = this::class[Tooltip::class]?.run {
                    Array(value.size) { i -> Text.component(value[i]) }
                } ?: return@forEach
            }

        if (count == 1) {
            enabled.title = Component.literal(this::class.tags[Override::class]?.value ?: name)
            enabled.section = null
        }

        if (!this.enabled)
            return

        events.register()
        tasks.resume()
        delegate?.setUserEnabled(true)
    }

    @ConfigCategory(Category.OVERLAYS)
    private inner class Delegate : WynntilsFeature() {
        val description: String = this::class.tags[Description::class]?.value ?: "No Description"

        override fun onDisable() {
            this@Feature.enabled = false
            0
        }

        override fun onEnable() {
            this@Feature.enabled = true
        }

        override fun getTranslatedName(): String =
            this@Feature.name

        override fun getTranslatedDescription(): String =
            description

        override fun getTranslationKeyName(): String =
            this@Feature.key

        override fun getShortName(): String =
            getTranslatedName().replace(" ", "").removeSuffix("Feature")
    }

    companion object {
        @Subscribe(priority = EventPriority.LOWEST)
        private fun ConnectedEvent.on() {
            Managers.Config.saveConfig()
        }
    }
}

/**
 * Sets the description on the Wynntils feature
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Description(val value: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Default(val value: State)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Overlays(vararg val value: KClass<out AbstractOverlay>)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Replaces(vararg val value: KClass<*>)

enum class State {
    ENABLED,
    DISABLED
}

private object FeatureEncoder : StringBasedEncoder<WynntilsFeature>() {
    override fun decode(
        obj: String,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): WynntilsFeature? = unsafe {
        Managers.Feature.getFeatureInstance(
            Class.forName(
                Base64.decode(obj)
            ) as Class<WynntilsFeature>
        )
    }.orNull()

    override fun encode(
        obj: WynntilsFeature,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): String =
        Base64.encode(obj.javaClass.toString())
}