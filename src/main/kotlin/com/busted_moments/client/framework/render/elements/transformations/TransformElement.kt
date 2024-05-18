@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.elements.transformations

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Floats
import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Vector2f
import org.joml.Vector3f
import javax.swing.text.html.HTML.Tag.P
import kotlin.experimental.ExperimentalTypeInference

abstract class TransformElement<CTX:  Context> : Element<CTX>() {
    lateinit var context: CTX
    private lateinit var pose: PoseStack

    fun reset() {
        pose.setIdentity()
    }

    fun translate(x: Float, y: Float) {
        pose.translate(x, y, 0f)
    }

    @OverloadResolutionByLambdaReturnType
    inline fun translate(block: (CTX) -> Floats) {
        block(context).also { translate(it.first, it.second) }
    }

    @JvmName("translateVec3")
    inline fun translate(block: (CTX) -> Vector3f) {
        block(context).also { translate(it.x, it.y) }
    }

    @JvmName("translateVec2")
    inline fun translate(block: (CTX) -> Vector2f) {
        block(context).also { translate(it.x, it.y) }
    }

    fun scale(x: Float, y: Float = x) {
        pose.scale(x, y, 0f)
    }

    @OverloadResolutionByLambdaReturnType
    inline fun scale(block: (CTX) -> Float) {
        block(context).also { scale(it) }
    }

    @JvmName("scaleFloats")
    inline fun scale(block: (CTX) -> Floats) {
        block(context).also { scale(it.first, it.second) }
    }

    @JvmName("scaleVec3")
    inline fun scale(block: (CTX) -> Vector3f) {
        block(context).also { scale(it.x, it.y) }
    }

    @JvmName("scaleVec2")
    inline fun scale(block: (CTX) -> Vector2f) {
        block(context).also { scale(it.x, it.y) }
    }

    fun rotate(rad: Float) {
        pose.last().pose().rotateZ(rad)
    }

    inline fun rotate(block: (CTX) -> Float) =
        rotate(block(context))

    override fun compute(ctx: CTX): Boolean = true

    override fun pre(ctx: CTX) {
        ctx.pose.pushPose()
        pose = ctx.pose
        context = ctx
    }

    override fun post(ctx: CTX) {
        ctx.pose.popPose()
    }
}

@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.transform(
    crossinline block: TransformElement<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : TransformElement<CTX>() {
            override fun draw(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("transformUnit")
inline fun <CTX : Context> Renderer<CTX>.transform(
    crossinline block: TransformElement<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : TransformElement<CTX>() {
            override fun draw(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}