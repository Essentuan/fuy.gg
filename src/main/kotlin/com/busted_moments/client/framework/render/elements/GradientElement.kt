@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.builder.RenderMode
import com.busted_moments.client.framework.render.builder.quad
import com.busted_moments.client.framework.render.builder.upload
import com.busted_moments.client.framework.render.builder.vertex
import com.busted_moments.client.framework.render.helpers.Context
import net.essentuan.esl.color.Color
import net.minecraft.client.renderer.GameRenderer
import kotlin.experimental.ExperimentalTypeInference


abstract class  GradientElement<CTX : Context> : RectElement<CTX>() {
    lateinit var from: Color
    lateinit var to: Color

    override fun draw(ctx: CTX): Boolean {
        upload(ctx) {
            quad {
                +RenderMode.BLEND
                shader(GameRenderer::getPositionColorShader)

                val x2 = x + width
                val y2 = y + height

                vertex(x, y, 0f, color = from)
                vertex(x, y2, 0f, color = to)
                vertex(x2, y2, 0f, color = from)
                vertex(x2, y, 0f, color = to)
            }
        }

        return true
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.gradient(
    crossinline block: GradientElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : GradientElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("gradientUnit")
inline fun <CTX : Context> Renderer<CTX>.gradient(
    crossinline block: GradientElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : GradientElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}