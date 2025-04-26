@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements.helpers

import com.busted_moments.client.framework.render.*
import com.busted_moments.client.framework.render.helpers.Context
import com.wynntils.utils.render.RenderUtils
import net.essentuan.esl.tuples.numbers.FloatPair
import kotlin.experimental.ExperimentalTypeInference

abstract class MaskElement<CTX : Context> : Element<CTX>(), MutableSizable {
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

    override fun draw(ctx: CTX): Boolean = true

    override fun pre(ctx: CTX) {
        ctx.buffer.endBatch()

        val pos = ctx.pose.apply(pos)
        val area = ctx.pose.applyScale(size)

        RenderUtils.enableScissor(
            ctx.graphics,
            pos.first.toInt(),
            pos.second.toInt(),
            area.first.toInt(),
            area.second.toInt()
        )
    }

    override fun post(ctx: CTX) {
        ctx.buffer.endBatch()
        RenderUtils.disableScissor(ctx.graphics)
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.mask(
    crossinline block: MaskElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : MaskElement<CTX>() {
            override fun compute(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("maskUnit")
inline fun <CTX : Context> Renderer<CTX>.mask(
    crossinline block: MaskElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : MaskElement<CTX>() {
            override fun compute(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}
