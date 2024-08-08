@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import com.wynntils.utils.render.buffered.CustomRenderType
import net.essentuan.esl.color.Color
import net.minecraft.client.renderer.GameRenderer
import org.joml.Matrix4f
import kotlin.experimental.ExperimentalTypeInference


abstract class GradientElement<CTX : Context> : RectElement<CTX>() {
    lateinit var from: Color
    lateinit var to: Color

    var blitOffset: Float = 0f

    override fun draw(ctx: CTX): Boolean {
        RenderSystem.enableBlend()
        RenderSystem.setShader(GameRenderer::getPositionColorShader)
        val builder = ctx.buffer.getBuffer(CustomRenderType.POSITION_COLOR_QUAD)

        val matrix: Matrix4f = ctx.pose.last().pose()

        val x2 = x + width
        val y2 = y + height

        val alphaA: Float = from.alpha / 255.0f
        val redA: Float = from.red / 255.0f
        val greenA: Float = from.green / 255.0f
        val blueA: Float = from.blue / 255.0f
        val alphaB: Float = to.alpha / 255.0f
        val redB: Float = to.red / 255.0f
        val greenB: Float = to.green / 255.0f
        val blueB: Float = to.blue / 255.0f

        builder.addVertex(matrix, x, y, blitOffset).setColor(redA, greenA, blueA, alphaA)
        builder.addVertex(matrix, x, y2, blitOffset).setColor(redB, greenB, blueB, alphaB)
        builder.addVertex(matrix, x2, y2, blitOffset).setColor(redB, greenB, blueB, alphaB)
        builder.addVertex(matrix, x2, y, blitOffset).setColor(redA, greenA, blueA, alphaA)

        RenderSystem.disableBlend()

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