package com.busted_moments.client.framework.features

import com.busted_moments.client.framework.artemis.config.link
import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.File
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.annotations.Section
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.MutableSizable
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.elements.TextBoxElement
import com.busted_moments.client.framework.render.elements.TextElement
import com.busted_moments.client.framework.render.helpers.IContext
import com.busted_moments.client.framework.render.helpers.Percentage
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.core.components.Managers
import com.wynntils.core.consumers.overlays.Overlay
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.core.consumers.overlays.OverlayPosition.AnchorSection
import com.wynntils.core.consumers.overlays.OverlaySize
import com.wynntils.core.consumers.overlays.RenderState
import com.wynntils.mc.event.ConnectionEvent.ConnectedEvent
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.other.Base64
import net.essentuan.esl.reflections.Constructors
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.client.DeltaTracker
import net.minecraft.client.renderer.MultiBufferSource
import kotlin.reflect.KClass

abstract class AbstractOverlay private constructor(
    val name: String,
    position: OverlayPosition,
    size: OverlaySize,
    horizontal: HorizontalAlignment,
    vert: VerticalAlignment
) : Overlay(
    position,
    size,
    horizontal,
    vert
), Renderer<AbstractOverlay.Context>, Storage {
    private constructor(definition: Define) : this(
        definition.name,
        OverlayPosition(
            definition.at.y,
            definition.at.x,
            definition.align.vert,
            definition.align.horizontal,
            definition.anchor
        ),
        OverlaySize(
            definition.size.width,
            definition.size.height
        ),
        definition.align.horizontal,
        definition.align.vert
    )

    constructor() : this(Define.find(Constructors.trace()))

    final override var first: Boolean = true
        private set

    private val elements = mutableListOf<Element<out Context>>()

    private lateinit var owner: Feature

    private var preview: Boolean = false

    @Persistent
    private var replaced: MutableSet<String> = mutableSetOf()

    override val category: String
        get() = this::class.tags[Category::class]?.value ?: owner.category

    override val section: String
        get() = this::class.tags[Section::class]?.value ?: owner.section

    override val file: String
        get() = this::class.tags[File::class]?.run {
            if (value.endsWith(".json"))
                value
            else
                "$value.json"
        } ?: owner.file

    @Subscribe
    private fun ConnectedEvent.on() {
        if (!shouldBeEnabled())
            return

        this@AbstractOverlay::class[Replaces::class]?.let {
            val replacing = it.value
                .asSequence()
                .map { cls -> Base64.encode(cls.java.name) }
                .toMutableSet()

            if (replacing != replaced) {
                replaced = mutableSetOf()

                for (cls in it.value) {
                    val instance = Managers.Overlay.getOverlay(cls.java as Class<Overlay>)
                    Managers.Overlay.disableOverlay(instance)
                    instance.getConfigOptionFromString("userEnabled").ifPresent {
                        (it as com.wynntils.core.persisted.config.Config<Boolean>).setValue(false)
                    }

                    replaced+= Base64.encode(cls.java.name)
                }
            }
        }
    }

    fun claim(feature: Feature) {
        check(!::owner.isInitialized)

        owner = feature
    }

    override fun ready() = link()
    
    protected fun MutableSizable.frame() {
        width = this@AbstractOverlay.width
        height = this@AbstractOverlay.height
        
        if (this is TextElement<*>) {
            horizontalAlignment = renderHorizontalAlignment
            verticalAlignment = renderVerticalAlignment
        }
    }
    
    private fun render(pose: PoseStack, buffers: MultiBufferSource.BufferSource, deltaTracker: DeltaTracker, window: Window, preview: Boolean) {
        this.pose = pose
        this.buffer = buffers
        this.deltaTracker = deltaTracker
        this.window = window
        this.preview = preview

        pose.pushPose()
        pose.translate(renderX, renderY, 0f)

        if (render(context))
            @Suppress("UNCHECKED_CAST")
            for (child in this)
                (child as Renderer<Context>).render(context)

        pose.popPose()

        first = elements.isEmpty()

        buffers.endBatch()
    }

    override fun render(p0: PoseStack, p1: MultiBufferSource, p2: DeltaTracker, p3: Window) {
        render(p0, p1 as MultiBufferSource.BufferSource, p2, p3, false)
    }

    override fun renderPreview(p0: PoseStack, p1: MultiBufferSource, p2: DeltaTracker, p3: Window) =
        render(p0, p1 as MultiBufferSource.BufferSource, p2, p3, true)

    override fun onConfigUpdate(p0: com.wynntils.core.persisted.config.Config<*>?) = Unit

    override fun iterator(): Iterator<Element<out Context>> =
        elements.toList().iterator()

    override fun plusAssign(child: Element<out Context>) =
        elements.plusAssign(child)

    override fun getTranslatedName(): String {
        return name
    }

    override fun getShortName(): String {
        return name.replace(" ", "")
    }

    override fun getDeclaringFeatureClassName(): String {
        return owner::class.simpleName!!
    }

    override fun getJsonName(): String {
        return Config.keyOf("${owner::class.simpleString()}).${this::class.simpleName}")
    }

    private lateinit var pose: PoseStack
    private lateinit var buffer: MultiBufferSource.BufferSource
    private lateinit var deltaTracker: DeltaTracker
    private lateinit var window: Window

    private val context = Context()

    inner class Context : IContext {
        val overlay: AbstractOverlay
            get() = this@AbstractOverlay

        val preview: Boolean
            get() = overlay.preview

        override val pose: PoseStack
            get() = this@AbstractOverlay.pose
        override val buffer: MultiBufferSource.BufferSource
            get() = this@AbstractOverlay.buffer
        override val deltaTracker: DeltaTracker
            get() = this@AbstractOverlay.deltaTracker
        override val window: Window
            get() = this@AbstractOverlay.window

    }
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Define(
    val name: String,
    val size: Size = Size(100f, 100f),
    val at: At = At(0f, 0f),
    val align: Align = Align(HorizontalAlignment.CENTER, VerticalAlignment.MIDDLE),
    val anchor: AnchorSection = AnchorSection.MIDDLE
) {
    companion object {
        fun find(cls: KClass<*>): Define {
            return cls.tags[Define::class] ?: throw NoSuchElementException("Missing overlay definition!")
        }
    }
}

annotation class Size(val height: Float, val width: Float)
annotation class At(val x: Float, val y: Float)
annotation class Align(val horizontal: HorizontalAlignment, val vert: VerticalAlignment)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RenderAt(val value: RenderState)

infix fun Percentage.of(overlay: Overlay): FloatPair = FloatPair(
    overlay.width * factor,
    overlay.height * factor
)

operator fun Percentage.plus(sizable: Overlay): FloatPair = FloatPair(
    sizable.width / factor,
    sizable.height / factor
)


operator fun Overlay.plus(pct: Percentage): FloatPair = FloatPair(
    width / pct.factor,
    height / pct.factor
)