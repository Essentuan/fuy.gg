@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements.transformations

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import kotlin.experimental.ExperimentalTypeInference

abstract class ResetElement<CTX : Context> : Element<CTX>() {
    override fun draw(ctx: CTX): Boolean = true

    override fun pre(ctx: CTX) {
        ctx.pose.pushPose()
        ctx.pose.setIdentity()
    }

    override fun post(ctx: CTX) =
        ctx.pose.popPose()
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.reset(
    crossinline block: ResetElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : ResetElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("resetUnit")
inline fun <CTX : Context> Renderer<CTX>.reset(
    crossinline block: ResetElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : ResetElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}