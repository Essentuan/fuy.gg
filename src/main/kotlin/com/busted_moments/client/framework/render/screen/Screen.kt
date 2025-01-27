package com.busted_moments.client.framework.render.screen

import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Renderer.Companion.contains
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.IContext
import com.busted_moments.client.framework.text.Text
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.reflections.Constructors
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.tasks
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.components.events.GuiEventListener
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import org.joml.Matrix4f
import kotlin.reflect.KClass

typealias McScreen = net.minecraft.client.gui.screens.Screen

abstract class Screen : McScreen(Title.find(Constructors.trace())), Renderer<Screen.Context> {
    final override var first: Boolean = true
        private set

    private val elements = mutableListOf<Element<out Context>>()

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.graphics = guiGraphics
        this.mouse = FloatPair(mouseX.toFloat(), mouseY.toFloat())

        this.deltaTracker = mc().deltaTracker
        this.window = mc().window

        if (render(context))
            @Suppress("UNCHECKED_CAST")
            for (child in this)
                (child as Renderer<Context>).render(context)

        first = elements.isEmpty()

        guiGraphics
    }

    override fun init() {
        events.register()
        tasks.resume()
    }

    final override fun removed() {
        events.unregister()
        tasks.suspend(true)

        close()
    }

    protected open fun close() = Unit

    override fun plusAssign(child: Element<out Context>) {
        elements.plusAssign(child)
    }

    @Suppress("UNCHECKED_CAST")
    fun register(listener: GuiEventListener) {
        (children() as MutableList<GuiEventListener>).add(listener)
    }

    override fun iterator(): Iterator<Element<out Context>> =
        elements.toList().iterator()

    private lateinit var graphics: GuiGraphics
    private var mouse: FloatPair = FloatPair.ZERO
    private lateinit var window: Window
    private lateinit var deltaTracker: DeltaTracker

    private val context = Context()

    inner class Context : IContext {
        val screen: Screen
            get() = this@Screen

        val graphics: GuiGraphics
            get() = this@Screen.graphics

        val mouse: FloatPair
            get() = this@Screen.mouse

        val mouseX: Float
            get() = this@Screen.mouse.first

        val mouseY: Float
            get() = this@Screen.mouse.second

        override val pose: PoseStack
            get() = this@Screen.graphics.pose()
        override val buffer: MultiBufferSource.BufferSource
            get() = this@Screen.graphics.bufferSource
        override val deltaTracker: DeltaTracker
            get() = this@Screen.deltaTracker
        override val window: Window
            get() = this@Screen.window

    }

    abstract class Widget : Element<Screen.Context>(), Sizable, GuiEventListener {
        private var added: Boolean = false
        private var focused: Boolean = false
        private var matrix = Matrix4f()

        final override fun isFocused(): Boolean = focused

        final override fun setFocused(focused: Boolean) {
            this.focused = focused
        }

        override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
            return contains(mouseX.toFloat(), mouseY.toFloat(), matrix)
        }

        final override fun draw(ctx: Context): Boolean {
            matrix = ctx.pose.last().pose()

            if (!added) {
                ctx.screen.register(this)
                added = true
            }

            return renderWidget(ctx)
        }

        protected abstract fun renderWidget(ctx: Context): Boolean
    }
}

fun McScreen.open() {
    mc().setScreen(this)
}

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Title(val value: String) {
    companion object {
        fun find(cls: KClass<*>): Component {
            return Text.component(
                cls.tags[Title::class]?.value ?: throw NoSuchElementException("Missing screen title!")
            )
        }
    }
}