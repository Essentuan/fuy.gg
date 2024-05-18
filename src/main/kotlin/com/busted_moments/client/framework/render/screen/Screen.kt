package com.busted_moments.client.framework.render.screen

import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.features.Overlay.Context
import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Floats
import com.busted_moments.client.framework.render.helpers.IContext
import com.busted_moments.client.framework.text.Text
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.reflections.Constructors
import net.essentuan.esl.reflections.extensions.get
import net.essentuan.esl.reflections.extensions.tags
import net.essentuan.esl.scheduling.tasks
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Component
import kotlin.reflect.KClass

typealias McScreen = net.minecraft.client.gui.screens.Screen

abstract class Screen : McScreen(Title.find(Constructors.trace())), Renderer<Screen.Context> {
    final override var first: Boolean = true
        private set

    private val elements = mutableListOf<Element<out Context>>()

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        this.graphics = guiGraphics
        this.mouse = Floats(mouseX.toFloat(), mouseY.toFloat())
        this.partialTicks = partialTick
        this.window = mc().window

        if (render(context))
            @Suppress("UNCHECKED_CAST")
            for (child in this)
                (child as Renderer<Context>).render(context)

        first = elements.isEmpty()
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

    override fun plusAssign(child: Element<out Context>) =
        elements.plusAssign(child)

    override fun iterator(): Iterator<Element<out Context>> =
        elements.iterator()

    private lateinit var graphics: GuiGraphics
    private var mouse: Floats = Floats.ZERO
    private lateinit var window: Window
    private var partialTicks: Float = 0f

    private val context = Context()

    inner class Context : IContext {
        val screen: Screen
            get() = this@Screen

        val graphics: GuiGraphics
            get() = this@Screen.graphics

        val mouse: Floats
            get() = this@Screen.mouse

        val mouseX: Float
            get() = this@Screen.mouse.first

        val mouseY: Float
            get() = this@Screen.mouse.second

        override val pose: PoseStack
            get() = this@Screen.graphics.pose()
        override val buffer: MultiBufferSource
            get() = this@Screen.graphics.bufferSource()
        override val partialTicks: Float
            get() = this@Screen.partialTicks
        override val window: Window
            get() = this@Screen.window

    }
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