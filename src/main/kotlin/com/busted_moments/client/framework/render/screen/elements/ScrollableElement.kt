package com.busted_moments.client.framework.render.screen.elements

import com.busted_moments.client.framework.render.*
import com.busted_moments.client.framework.render.screen.Screen
import com.busted_moments.client.framework.text.Text.invoke
import com.wynntils.utils.render.RenderUtils
import me.shedaniel.clothconfig2.impl.EasingMethod
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.Optionull.first
import net.minecraft.client.gui.components.events.GuiEventListener
import kotlin.experimental.ExperimentalTypeInference

abstract class ScrollableElement : Element<Screen.Context>(), MutableSizable, GuiEventListener {
    private var added = false

    private lateinit var _texture: Texture

    var texture: Texture
        set(value) {
            _texture = value
        }
        get() = _texture

    var sliderPos: FloatPair = FloatPair.ZERO

    var sliderOriginX: Float
        get() = sliderPos.first
        set(value) {
            sliderPos = sliderPos.copy(value)
        }

    var sliderOriginY: Float
        get() = sliderPos.second
        set(value) {
            sliderPos = sliderPos.copy(second = value)
        }

    var sliderHeight: Float = 0f

    private val sliderY: Float
        get() = (sliderOriginY + (progress * (sliderHeight - texture.height))).toFloat()

    var size: FloatPair = FloatPair.ZERO

    override var width: Float
        get() = size.first
        set(value) {
            size = size.copy(value)
        }
    override var height: Float
        get() = size.second
        set(value) {
            size = size.copy(second = value)
        }

    var progress: Double = 0.0
        private set(value) {
            field = value.coerceIn(0.0..1.0)
        }

    var intensity: Double = 1.0

    var easing: EasingMethod = EasingMethod.EasingMethodImpl.LINEAR

    private var dragging: Pair<Float, Double>? = null
    private var animation: Animation? = null

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && dragging == null && mouseX >= sliderOriginX && mouseX <= sliderOriginX + texture.width) {
            if (mouseY > sliderY && mouseY <= (sliderY + texture.height)) {
                dragging = sliderY + texture.height / 2f to mouseY

                return true
            } else if (mouseY > sliderOriginY && mouseY <= (sliderOriginY + sliderHeight) + texture.height) {
                progress = (mouseY - sliderOriginY - texture.height / 2f) / height
                animation = null
                dragging = sliderY to mouseY

                return true
            }
        }

        return false
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (button == 0 && dragging != null) {
            var y = dragging!!.first + mouseY - dragging!!.second
            progress = (y - sliderOriginY - texture.height / 2f) / height

            return true
        }

        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && dragging != null) {
            dragging = null

            return true
        }

        return false
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scrollX: Double, scrollY: Double): Boolean {
        animation = Animation(
            ((scrollY / sliderHeight) * -1 * intensity),
            200
        )

        dragging = null

        return true
    }

    override fun pre(ctx: Screen.Context) {
        animation?.update()

        ctx.buffer.endBatch()

        val pos = ctx.pose.apply(pos)
        val area = ctx.pose.applyScale(size)

        RenderUtils.enableScissor(
            pos.first.toInt(),
            pos.second.toInt(),
            area.first.toInt(),
            area.second.toInt()
        )
    }

    override fun draw(ctx: Screen.Context): Boolean {
        if (!added) {
            ctx.screen.register(this)
            added = true
        }

        return true
    }

    override fun post(ctx: Screen.Context) {
        ctx.buffer.endBatch()
        RenderUtils.disableScissor()

        texture.render(
            ctx.pose,
            ctx.buffer,
            sliderOriginX,
            (sliderOriginY + (progress * sliderHeight)).toFloat()
        )
    }

    override fun setFocused(focused: Boolean) = Unit

    override fun isFocused(): Boolean =
        false

    private inner class Animation(
        val amount: Double,
        val duration: Long,
        val start: Double = progress,
        val at: Long = System.currentTimeMillis()
    ) {
        val current: Double
            get() = (System.currentTimeMillis() - at) / duration.toDouble()

        fun update() {
            if (System.currentTimeMillis() > at + duration) {
                animation = null
                progress = start + amount
            } else {
                progress = start + easing.apply(current) * amount
            }
        }
    }
}

@OverloadResolutionByLambdaReturnType
@OptIn(ExperimentalTypeInference::class)
inline fun Renderer<Screen.Context>.scrollable(
    crossinline block: ScrollableElement.(Screen.Context) -> Boolean
) {
    if (first)
        this += object : ScrollableElement() {
            override fun compute(ctx: Screen.Context): Boolean = block(ctx)
        }
}

@JvmName("scrollableUnit")
inline fun Renderer<Screen.Context>.scrollable(
    crossinline block: ScrollableElement.(Screen.Context) -> Unit
) {
    if (first)
        this += object : ScrollableElement() {
            override fun compute(ctx: Screen.Context): Boolean {
                block(ctx)

                return true
            }
        }
}