@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.artemis.esl
import com.busted_moments.client.framework.render.*
import com.busted_moments.client.framework.render.helpers.Context
import com.wynntils.core.text.StyledText
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.tuples.numbers.FloatPair
import kotlin.experimental.ExperimentalTypeInference

abstract class TextElement<CTX : Context> : Element<CTX>(), MutableSizable {
    open lateinit var split: Split

    /**
     * The text to be rendered
     *
     * Note: This may be empty when setting [TextElement.split]!
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

    var size = FloatPair(10000f, 10000f)
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