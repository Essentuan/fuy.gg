package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.Texture
import com.busted_moments.client.framework.render.helpers.Context
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color
import net.essentuan.esl.tuples.numbers.FloatPair
import kotlin.experimental.ExperimentalTypeInference

abstract class TextureElement<CTX : Context> : Element<CTX>(), Sizable {
    private lateinit var _texture: Texture

    var texture: Texture
        set(value) {
            _texture = value
            size = FloatPair(
                value.width,
                value.height
            )
        }
        get() = _texture

    var ux: Int = 0
    var uy: Int = 0

    var color: Color = CustomColor.NONE.esl

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

    override fun draw(ctx: CTX): Boolean {
        texture.render(
            ctx.pose,
            ctx.buffer,
            x,
            y,
            width = width,
            height = height,
            ux = ux,
            uy = uy,
            color = color
        )

        return true
    }
}

@OverloadResolutionByLambdaReturnType
@OptIn(ExperimentalTypeInference::class)
inline fun <CTX : Context> Renderer<CTX>.texture(
    crossinline block: TextureElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : TextureElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("textureUnit")
inline fun <CTX : Context> Renderer<CTX>.texture(
    crossinline block: TextureElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : TextureElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}