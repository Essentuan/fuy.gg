@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.screen.elements

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Positional
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.screen.Screen
import org.joml.Matrix4f
import org.joml.Vector3f
import kotlin.experimental.ExperimentalTypeInference

abstract class HoverElement(
    private val obj: Any
) : Element<Screen.Context>() {
    override fun draw(ctx: Screen.Context): Boolean =
        ctx.isInside(obj, ctx.pose.last().pose())
}

private val v1 = Vector3f()
private val v2 = Vector3f()

private fun Screen.Context.leftOfEdge(x1: Float, y1: Float, x2: Float, y2: Float, matrix4f: Matrix4f): Boolean {
    v1.x = x1
    v1.y = y1
    v1.z = 0f

    v2.x = x2
    v2.y = y2
    v2.z = 0f

    matrix4f.transformPosition(v1, v1)
    matrix4f.transformPosition(v2, v2)

    return (((v2.x - v1.x) * (mouseY - v1.y)) - ((mouseX - v1.x) * (v2.y - v1.y))) <= 0
}

private fun Screen.Context.isInside(obj: Any, matrix4f: Matrix4f): Boolean {
    val pos = obj as Positional
    val size = obj as Sizable

    val x1 = pos.x
    val y1 = pos.y
    val x2 = pos.x
    val y2 = pos.y + size.height
    val x3 = pos.x + size.width
    val y3 = pos.y + size.height
    val x4 = pos.x + size.width
    val y4 = pos.y

    return leftOfEdge(x1, y1, x2, y2, matrix4f) &&
            leftOfEdge(x2, y2, x3, y3, matrix4f) &&
            leftOfEdge(x3, y3, x4, y4, matrix4f) &&
            leftOfEdge(x4, y4, x1, y1, matrix4f)
}

@OverloadResolutionByLambdaReturnType
inline fun <T> T.hover(
    crossinline block: HoverElement.(Screen.Context) -> Boolean
) where T : Renderer<Screen.Context>, T: Positional, T: Sizable {
    if (first)
        this += object : HoverElement(this@hover) {
            override fun compute(ctx: Screen.Context): Boolean = block(ctx)
        }
}

@JvmName("hoverUnit")
inline fun <T> T.hover(
    crossinline block: HoverElement.(Screen.Context) -> Unit
) where T : Renderer<Screen.Context>, T: Positional, T: Sizable {
    if (first)
        this += object : HoverElement(this@hover) {
            override fun compute(ctx: Screen.Context): Boolean {
                block(ctx)

                return true
            }
        }
}
