@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.artemis.artemis
import com.busted_moments.client.framework.artemis.esl
import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.Split
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Floats
import com.busted_moments.client.framework.render.text
import com.wynntils.core.text.StyledText
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.render.FontRenderer
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import kotlin.experimental.ExperimentalTypeInference

abstract class TextElement<CTX : Context> : Element<CTX>(), Sizable {
    lateinit var split: Split

    /**
     * The text to be rendered
     *
     * NOTE: This may be empty when using [split]!
     */
    var text: StyledText = StyledText.EMPTY
        set(value) {
            split = TextRenderer.split(value, maxWidth.coerceAtMost(width) - additionalWidth)
            field = value
        }

    protected open val additionalWidth: Float
        get() = 0f


    var style = TextShadow.OUTLINE
    var color = CommonColors.WHITE.esl

    var size = Floats(10000f, 10000f)
    var maxWidth: Float = 0f

    var horizontalAlignment = HorizontalAlignment.LEFT
    var verticalAlignment = VerticalAlignment.TOP

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

    override fun draw(ctx: CTX): Boolean {
        ctx.text(
            split,
            ctx.buffer,
            x,
            y,
            width,
            height,
            horizontalAlignment,
            verticalAlignment,
            style,
            1f
        )

        return true
    }
}

class BoxElement {

}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.text(
    crossinline block: TextElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : TextElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("textUnit")
inline fun <CTX : Context> Renderer<CTX>.text(
    crossinline block: TextElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : TextElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}