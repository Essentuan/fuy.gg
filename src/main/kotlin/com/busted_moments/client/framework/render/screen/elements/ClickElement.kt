@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.screen.elements

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Positional
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Renderer.Companion.contains
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.screen.Screen
import net.minecraft.client.gui.components.events.GuiEventListener
import org.joml.Matrix4f
import kotlin.experimental.ExperimentalTypeInference

private val EMPTY = Matrix4f()

abstract class ClickElement<T>(
    private val transform: Boolean,
    private val obj: T
) : Element<Screen.Context>(), GuiEventListener where T : Positional, T : Sizable {
    private var added = false
    private var matrix4f: Matrix4f = EMPTY

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean =
        obj.contains(mouseX.toFloat(), mouseY.toFloat(), matrix4f) && click(mouseX, mouseY, button)

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean =
        obj.contains(mouseX.toFloat(), mouseY.toFloat(), matrix4f)

    override fun isFocused(): Boolean = false

    override fun setFocused(focused: Boolean) = Unit

    protected abstract fun click(mouseX: Double, mouseY: Double, button: Int): Boolean

    override fun draw(ctx: Screen.Context): Boolean = true

    override fun compute(ctx: Screen.Context): Boolean {
        if (transform)
            matrix4f = ctx.pose.last().pose().clone() as Matrix4f

        if (!added) {
            ctx.screen.register(this)
            added = true
        }

        return true
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <T> T.click(
    transform: Boolean = true,
    crossinline block: ClickElement<T>.(mouseX: Double, mouseY: Double, button: Int) -> Boolean
) where T : Renderer<Screen.Context>, T : Positional, T : Sizable {
    if (first)
        this += object : ClickElement<T>(transform, this@click) {
            override fun click(mouseX: Double, mouseY: Double, button: Int): Boolean = block(mouseX, mouseY, button)
        }
}
