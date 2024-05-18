package com.busted_moments.client.framework.features

import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Floats
import com.busted_moments.client.framework.render.helpers.IContext
import com.busted_moments.client.framework.render.helpers.Percentage
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.core.consumers.overlays.OverlayPosition.AnchorSection
import com.wynntils.core.consumers.overlays.OverlaySize
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.reflections.Constructors
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.simpleString
import net.essentuan.esl.reflections.extensions.tags
import net.minecraft.client.renderer.MultiBufferSource
import kotlin.reflect.KClass

private typealias Artemis = com.wynntils.core.consumers.overlays.Overlay

abstract class Overlay private constructor(
    val name: String,
    position: OverlayPosition,
    size: OverlaySize,
    horizontal: HorizontalAlignment,
    vert: VerticalAlignment
) : Artemis(
    position,
    size,
    horizontal,
    vert
), Renderer<Overlay.Context> {
    private constructor(definition: Define) : this(
        definition.name,
        OverlayPosition(
            definition.at.x,
            definition.at.y,
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

    fun claim(feature: Feature) {
        check(!::owner.isInitialized)

        owner = feature
    }

    protected abstract fun render(ctx: Context, preview: Boolean): Boolean

    final override fun render(ctx: Context) =
        render(ctx, false)

    private fun render(pose: PoseStack, buffers: MultiBufferSource, partialTicks: Float, window: Window, preview: Boolean) {
        this.pose = pose
        this.buffer = buffers
        this.partialTicks = partialTicks
        this.window = window

        pose.pushPose()
        pose.translate(renderX, renderY, 0f)

        if (render(context, preview))
            @Suppress("UNCHECKED_CAST")
            for (child in this)
                (child as Renderer<Context>).render(context)

        pose.popPose()

        first = elements.isEmpty()
    }

    override fun render(p0: PoseStack, p1: MultiBufferSource, p2: Float, p3: Window) =
        render(p0, p1, p2, p3, false)

    override fun renderPreview(p0: PoseStack, p1: MultiBufferSource, p2: Float, p3: Window) =
        render(p0, p1, p2, p3, true)

    override fun onConfigUpdate(p0: com.wynntils.core.persisted.config.Config<*>?) = Unit

    override fun iterator(): Iterator<Element<out Context>> =
        elements.iterator()

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
    private lateinit var buffer: MultiBufferSource
    private var partialTicks: Float = 0f
    private lateinit var window: Window

    private val context = Context()

    inner class Context : IContext {
        val overlay: Overlay
            get() = this@Overlay

        override val pose: PoseStack
            get() = this@Overlay.pose
        override val buffer: MultiBufferSource
            get() = this@Overlay.buffer
        override val partialTicks: Float
            get() = this@Overlay.partialTicks
        override val window: Window
            get() = this@Overlay.window
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

infix fun Percentage.of(overlay: Artemis): Floats = Floats(
    overlay.width * factor,
    overlay.height * factor
)

operator fun Percentage.plus(sizable: Artemis): Floats = Floats(
    sizable.width / factor,
    sizable.height / factor
)


operator fun Artemis.plus(pct: Percentage): Floats = Floats(
    width / pct.factor,
    height / pct.factor
)