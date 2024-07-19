package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.helpers.Context
import com.mojang.blaze3d.vertex.VertexConsumer
import com.wynntils.utils.render.buffered.CustomRenderType
import net.essentuan.esl.color.Color

fun VertexConsumer.setColor(color: Color): VertexConsumer =
    setColor(color.red, color.green, color.blue, color.alpha)

fun Context.line(
    color: Color,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    width: Float
) {
    var x1 = x1
    var y1 = y1
    var x2 = x2
    var y2 = y2
    val matrix = pose.last().pose()
    val halfWidth = width / 2.0f
    val buffer = buffer.getBuffer(CustomRenderType.POSITION_COLOR_TRIANGLE_STRIP)

    var tmp: Float
    when {
        x1 == x2 -> {
            if (y2 < y1) {
                tmp = y1
                y1 = y2
                y2 = tmp
            }

            buffer.addVertex(matrix, x1 - halfWidth, y1, 0f).setColor(color)
            buffer.addVertex(matrix, x2 - halfWidth, y2, 0f).setColor(color)
            buffer.addVertex(matrix, x1 + halfWidth, y1, 0f).setColor(color)
            buffer.addVertex(matrix, x2 + halfWidth, y2, 0f).setColor(color)
        }

        y1 == y2 -> {
            if (x2 < x1) {
                tmp = x1
                x1 = x2
                x2 = tmp
            }

            buffer.addVertex(matrix, x1, y1 - halfWidth, 0f).setColor(color)
            buffer.addVertex(matrix, x1, y1 + halfWidth, 0f).setColor(color)
            buffer.addVertex(matrix, x2, y2 - halfWidth, 0f).setColor(color)
            buffer.addVertex(matrix, x2, y2 + halfWidth, 0f).setColor(color)
        }

        x1 < x2 && y1 < y2 || x2 < x1 && y2 < y1 -> {
            if (x2 < x1) {
                tmp = x1
                x1 = x2
                x2 = tmp
                tmp = y1
                y1 = y2
                y2 = tmp
            }

            buffer.addVertex(matrix, x1 + halfWidth, y1 - halfWidth, 0f).setColor(color)

            buffer.addVertex(matrix, x1 - halfWidth, y1 + halfWidth, 0f).setColor(color)

            buffer.addVertex(matrix, x2 + halfWidth, y2 - halfWidth, 0f).setColor(color)

            buffer.addVertex(matrix, x2 - halfWidth, y2 + halfWidth, 0f).setColor(color)
        }

        else -> {
            if (x1 < x2) {
                tmp = x1
                x1 = x2
                x2 = tmp
                tmp = y1
                y1 = y2
                y2 = tmp
            }

            buffer.addVertex(matrix, x1 + halfWidth, y1 + halfWidth, 0f).setColor(color)
            buffer.addVertex(matrix, x1 - halfWidth, y1 - halfWidth, 0f).setColor(color)
            buffer.addVertex(matrix, x2 + halfWidth, y2 + halfWidth, 0f).setColor(color)
            buffer.addVertex(matrix, x2 - halfWidth, y2 - halfWidth, 0f).setColor(color)
        }
    }
}