package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.Vec3f
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Percentage
import com.busted_moments.client.framework.render.screen.Screen
import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.world.phys.Vec3
import org.joml.Matrix4f
import org.joml.Vector3f

typealias Vec3f = Vector3f

interface Renderer<CTX : Context> {
    val first: Boolean

    /**
     * @return `true` if this element was rendered
     */
    fun render(ctx: CTX): Boolean

    operator fun plusAssign(child: Element<out CTX>)

    operator fun iterator(): Iterator<Element<out CTX>>

    companion object {
        private val v1 = Vec3f()
        private val v2 = Vec3f()

        private fun leftOfEdge(
            px: Float,
            py: Float,
            x1: Float,
            y1: Float,
            x2: Float,
            y2: Float,
            matrix4f: Matrix4f
        ): Boolean {
            v1.x = x1
            v1.y = y1
            v1.z = 0f

            v2.x = x2
            v2.y = y2
            v2.z = 0f

            matrix4f.transformPosition(v1, v1)
            matrix4f.transformPosition(v2, v2)

            return (((v2.x - v1.x) * (py - v1.y)) - ((px - v1.x) * (v2.y - v1.y))) <= 0
        }


        fun <T> T.contains(
            x: Float,
            y: Float,
            matrix4f: Matrix4f
        ): Boolean where T : Positional, T : Sizable {
            val x1 = this.x
            val y1 = this.y
            val x2 = this.x
            val y2 = this.y + height
            val x3 = this.x + width
            val y3 = this.y + height
            val x4 = this.x + width
            val y4 = this.y

            return leftOfEdge(x, y, x1, y1, x2, y2, matrix4f) &&
                    leftOfEdge(x, y, x2, y2, x3, y3, matrix4f) &&
                    leftOfEdge(x, y, x3, y3, x4, y4, matrix4f) &&
                    leftOfEdge(x, y, x4, y4, x1, y1, matrix4f)
        }

        fun <T> T.contains(
            x: Float,
            y: Float,
            poseStack: PoseStack
        ): Boolean where T : Positional, T : Sizable =
            contains(x, y, poseStack.last().pose())


        operator fun <T> T.contains(ctx: Screen.Context): Boolean where T : Positional, T : Sizable =
            contains(ctx.mouseX, ctx.mouseY, ctx.pose)
    }
}

private val CONTAINER = Vec3f()

fun PoseStack.applyScale(floats: FloatPair): FloatPair {
    last().pose().getScale(CONTAINER)

    return FloatPair(
        floats.first * CONTAINER.x,
        floats.second * CONTAINER.y
    )
}

fun PoseStack.apply(x: Float, y: Float): FloatPair {
    CONTAINER.x = x
    CONTAINER.y = y
    CONTAINER.z = 0f

    apply(CONTAINER)

    return FloatPair(CONTAINER.x, CONTAINER.y)
}

fun PoseStack.apply(floats: FloatPair): FloatPair =
    apply(floats.first, floats.second)

fun PoseStack.apply(vec3: Vec3f, dest: Vec3f = vec3) {
    last().pose().transformPosition(vec3, dest)
}

infix fun Percentage.of(sizeable: Window): FloatPair = FloatPair(
    sizeable.guiScaledWidth * factor,
    sizeable.guiScaledHeight * factor
)

operator fun Percentage.plus(sizable: Window): FloatPair = FloatPair(
    sizable.guiScaledWidth / factor,
    sizable.guiScaledHeight / factor
)

operator fun Window.plus(pct: Percentage): FloatPair = FloatPair(
    width / pct.factor,
    height / pct.factor
)