@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Padding
import com.busted_moments.client.framework.render.text
import com.wynntils.utils.render.buffered.BufferedRenderUtils
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.color.Color
import kotlin.experimental.ExperimentalTypeInference

abstract class TextBoxElement<CTX : Context> : TextElement<CTX>() {
    var background: Color = HALF_BLACK
    var padding: Padding = Padding(0f, 0f, 0f, 0f)

    override val additionalWidth: Float
        get() = padding.left + padding.right

    fun resize() {
        width = split.width + padding.left + padding.right
        height = split.height + padding.top + padding.bottom
    }

    override fun draw(ctx: CTX): Boolean {
        val boxWidth = split.width + padding.left + padding.right
        val boxHeight = split.height + padding.top + padding.bottom

        val boxX: Float
        val boxY: Float

        val textX: Float
        val textY: Float

        val textWidth: Float
        val textHeight: Float

        when(horizontalAlignment) {
            HorizontalAlignment.LEFT -> {
                boxX = x
                textX = x + padding.left
                textWidth = width - (padding.left + padding.right)
            }
            HorizontalAlignment.CENTER -> {
                boxX = x + (width/2f - boxWidth/2f)
                textX = x
                textWidth = width
            }
            HorizontalAlignment.RIGHT -> {
                boxX = x + (width - boxWidth)
                textX = x + padding.left
                textWidth = width - (padding.left + padding.right)
            }
        }

        when(verticalAlignment) {
            VerticalAlignment.TOP -> {
                boxY = y
                textY = y + padding.top
                textHeight = height - (padding.top + padding.bottom)
            }
            VerticalAlignment.MIDDLE -> {
                boxY = y + (height/2f - boxHeight/2f)
                textY = y + padding.top
                textHeight = height
            }
            VerticalAlignment.BOTTOM -> {
                boxY = y + (height - boxHeight)
                textY = y + padding.top
                textHeight = height
            }
        }

        BufferedRenderUtils.drawRect(
            ctx.pose,
            ctx.buffer,
            background.wynntils,
            boxX,
            boxY,
            0f,
            boxWidth,
            boxHeight
        )

        ctx.text(
            split,
            textX,
            textY,
            textWidth,
            textHeight,
            horizontalAlignment,
            verticalAlignment,
            style,
            1f
        )

        return true
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.textbox(
    crossinline block: TextBoxElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : TextBoxElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("textBoxUnit")
inline fun <CTX : Context> Renderer<CTX>.textbox(
    crossinline block: TextBoxElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : TextBoxElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}