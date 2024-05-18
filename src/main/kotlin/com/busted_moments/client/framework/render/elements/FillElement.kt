@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.artemis.artemis
import com.busted_moments.client.framework.artemis.esl
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.render.buffered.BufferedRenderUtils
import net.essentuan.esl.color.Color
import kotlin.experimental.ExperimentalTypeInference

val HALF_BLACK: Color = CommonColors.BLACK.withAlpha(0.5f).esl

abstract class FillElement<CTX : Context> : RectElement<CTX>() {
    lateinit var color: Color
    var z: Float = 0f

    override fun draw(ctx: CTX): Boolean {
        BufferedRenderUtils.drawRect(
            ctx.pose,
            ctx.buffer,
            color.artemis,
            x,
            y,
            z,
            width,
            height
        )

        return true
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.fill(
    crossinline block: FillElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : FillElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("fillUnit")
inline fun <CTX : Context> Renderer<CTX>.fill(
    crossinline block: FillElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : FillElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}