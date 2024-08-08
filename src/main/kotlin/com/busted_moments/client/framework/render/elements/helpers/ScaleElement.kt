@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements.helpers

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import kotlin.experimental.ExperimentalTypeInference

abstract class ScaleElement<CTX : Context>(var scale: Float = 1f) : Element<CTX>() {
    override fun draw(ctx: CTX): Boolean = true

    override fun pre(ctx: CTX) {
        ctx.pose.pushPose()
        ctx.pose.scale(scale, scale, 0f)
        ctx.pose.translate(x, y, 0f)
    }

    override fun post(ctx: CTX) {
        ctx.pose.popPose()
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.scale(
    scale: Float = 1f,
    crossinline block: ScaleElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : ScaleElement<CTX>(scale) {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("scaleUnit")
inline fun <CTX : Context> Renderer<CTX>.scale(
    scale: Float = 1f,
    crossinline block: ScaleElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : ScaleElement<CTX>(scale) {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}

