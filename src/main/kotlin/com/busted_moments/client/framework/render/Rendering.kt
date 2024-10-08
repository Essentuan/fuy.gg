package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.builder.quad
import com.busted_moments.client.framework.render.builder.upload
import com.busted_moments.client.framework.render.builder.vertex
import com.busted_moments.client.framework.render.helpers.Context
import net.essentuan.esl.color.Color

fun Context.line(
    color: Color,
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    width: Float
) {
    upload(this) {
        var x1 = x1
        var y1 = y1
        var x2 = x2
        var y2 = y2
        val halfWidth = width / 2.0f
        var tmp: Float

        when {
            x1 == x2 -> {
                if (y2 < y1) {
                    tmp = y1
                    y1 = y2
                    y2 = tmp
                }

                quad {
                    vertex(x1 - halfWidth, y1, 0f, color = color)
                    vertex(x2 - halfWidth, y2, 0f, color = color)
                    vertex(x1 + halfWidth, y1, 0f, color = color)
                    vertex(x2 + halfWidth, y2, 0f, color = color)
                }
            }

            y1 == y2 -> {
                if (x2 < x1) {
                    tmp = x1
                    x1 = x2
                    x2 = tmp
                }

                quad {
                    vertex(x1, y1 - halfWidth, 0f, color = color)
                    vertex(x1, y1 + halfWidth, 0f, color = color)
                    vertex(x2, y2 - halfWidth, 0f, color = color)
                    vertex(x2, y2 + halfWidth, 0f, color = color)
                }
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

                quad {
                    vertex(x1 + halfWidth, y1 - halfWidth, 0f, color = color)
                    vertex(x1 - halfWidth, y1 + halfWidth, 0f, color = color)
                    vertex(x2 + halfWidth, y2 - halfWidth, 0f, color = color)
                    vertex(x2 - halfWidth, y2 + halfWidth, 0f, color = color)
                }
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

                quad {
                    vertex(x1 + halfWidth, y1 + halfWidth, 0f, color = color)
                    vertex(x1 - halfWidth, y1 - halfWidth, 0f, color = color)
                    vertex(x2 + halfWidth, y2 + halfWidth, 0f, color = color)
                    vertex(x2 - halfWidth, y2 - halfWidth, 0f, color = color)
                }
            }
        }
    }
}