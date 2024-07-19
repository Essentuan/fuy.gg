package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.TextPart
import com.busted_moments.client.framework.text.text
import com.mojang.blaze3d.vertex.Tesselator
import com.wynntils.core.text.StyledText
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.render.FontRenderer
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.color.Color
import net.minecraft.client.gui.Font
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.network.chat.Style
import java.util.Collections
import kotlin.math.max

private val EMPTY_TEXT = Split(Collections.emptyList())

object TextRenderer {
    val font: Font
        get() = FontRenderer.getInstance().font

    fun width(codePoint: Int, style: Style): Float =
        font.splitter.widthProvider.getWidth(codePoint, style)

    fun width(part: TextPart): Float =
        split(Text {
            +part
        }).width

    fun split(text: StyledText, maxWidth: Float = 0.0f): Split {
        val simple = maxWidth <= 0.0f

        if (text.isEmpty)
            return EMPTY_TEXT

        val split = Split()
        var line = Split.Line()

        for (part in text) {
            val string = part.text

            if (string.isEmpty())
                continue

            val style = part.partStyle.style

            var width = 0f

            var start = 0
            var cursor = 0

            fun next() {
                line += Split.Computed(
                    TextPart(
                        string.substring(start, cursor),
                        part.partStyle
                    ),
                    width
                )

                split += line
                line = Split.Line()

                width = 0f
                start = cursor
            }

            fun append(code: Int, size: Int) {
                val charWidth = width(code, style)

                if (!simple && line.width + width + charWidth > maxWidth)
                    next()

                width += charWidth
                cursor += size
            }

            while (cursor < string.length) {
                val char = string[cursor]

                when {
                    char == '\n' -> {
                        next()
                        cursor++
                        start++
                    }

                    Character.isHighSurrogate(char) -> {
                        if (cursor + 1 >= string.length)
                            break

                        val low = string[cursor + 1]

                        if (Character.isLowSurrogate(low)) {
                            cursor++
                            continue
                        }

                        append(Character.toCodePoint(char, low), 2)
                    }

                    else -> append(char.code, 1)
                }
            }

            if (start == cursor)
                continue

            if (!simple && line.width + width > maxWidth)
                next()
            else {
                line += Split.Computed(
                    TextPart(
                        string.substring(start, cursor),
                        part.partStyle
                    ),
                    width
                )
            }
        }

        if (line.isNotEmpty())
            split += line

        return split
    }
}

class Split(
    private val lines: MutableList<Line> = mutableListOf()
) : List<Split.Line> by lines {
    var width: Float = 0.0f
        private set

    val height: Float
        get() = size * TextRenderer.font.lineHeight.toFloat()

    internal operator fun plusAssign(line: Line) {
        lines += line
        width = max(width, line.width)
    }

    class Line(
        private val parts: MutableList<Computed> = mutableListOf()
    ) : List<Computed> by parts {
        var width: Float = 0.0f
            private set

        operator fun plusAssign(part: Computed) {
            parts += part
            width += part.width
        }
    }

    data class Computed(
        val text: TextPart,
        val width: Float
    )
}

private fun Context.batch(
    font: Font,
    part: TextPart,
    x: Float,
    y: Float,
    dropShadow: Boolean
) {
    font.drawInBatch(
        part,
        x,
        y,
        part.color,
        dropShadow,
        pose.last().pose(),
        buffer,
        Font.DisplayMode.SEE_THROUGH,
        0,
        15728880
    )
}

private val SHADOW_DATA = CommonColors.BLACK.asInt().toLong() shl 32

private fun Context.render(
    font: Font,
    part: TextPart,
    x: Float,
    y: Float,
    style: TextShadow
) {
    if (style == TextShadow.OUTLINE) {
        val before = part.color
        part.color = CommonColors.BLACK.withAlpha((before ushr 24) and 255).asInt()

        batch(font, part, x - 1f, y, false)
        batch(font, part,  x + 1, y, false)
        batch(font, part, x, y - 1f, false)
        batch(font, part, x, y + 1f, false)

        part.color = before
    }

    batch(font, part, x, y, style == TextShadow.NORMAL)
}

private fun Context.draw(
    font: Font,
    part: TextPart,
    x: Float,
    y: Float,
    style: TextShadow
) {
    if (font.isBidirectional) {
        val before = part.string
        part.string = font.bidirectionalShaping(before)

        render(font, part, x, y, style)

        part.string = before
    } else
        render(font, part, x, y, style)
}

private fun Context.line(
    font: Font,
    line: Split.Line,
    x: Float,
    y: Float,
    style: TextShadow
) {
    var x = x

    for (part in line) {
        draw(
            font,
            part.text,
            x,
            y,
            style
        )

        x += part.width
    }
}

fun Context.text(
    text: Split,
    x: Float,
    y: Float,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.LEFT,
    verticalAlignment: VerticalAlignment = VerticalAlignment.TOP,
    style: TextShadow = TextShadow.OUTLINE,
    scale: Float = 1f
) {
    val font = TextRenderer.font

    pose.pushPose()
    pose.translate(x, y, 0f)
    pose.scale(scale, scale, 0f)

    for (i in text.indices) {
        val line = text[i]

        val renderX: Float = when (horizontalAlignment) {
            HorizontalAlignment.LEFT -> 0f
            HorizontalAlignment.CENTER -> -(line.width / 2f)
            HorizontalAlignment.RIGHT -> -line.width
        }

        val renderY: Float = when (verticalAlignment) {
            VerticalAlignment.TOP -> 0f
            VerticalAlignment.MIDDLE -> -4f
            VerticalAlignment.BOTTOM -> -9f
        } + (9f * i)

        line(
            font,
            line,
            renderX,
            renderY,
            style
        )
    }

    pose.popPose()
}

fun Context.text(
    text: StyledText,
    x: Float,
    y: Float,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.LEFT,
    verticalAlignment: VerticalAlignment = VerticalAlignment.TOP,
    style: TextShadow = TextShadow.OUTLINE,
    scale: Float = 1f
) = text(
    TextRenderer.split(text),
    x,
    y,
    horizontalAlignment,
    verticalAlignment,
    style,
    scale
)

fun Context.text(
    text: Split,
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    horizontalAlignment: HorizontalAlignment = HorizontalAlignment.LEFT,
    verticalAlignment: VerticalAlignment = VerticalAlignment.TOP,
    style: TextShadow = TextShadow.OUTLINE,
    scale: Float = 1f
) = text(
    text,
    when (horizontalAlignment) {
        HorizontalAlignment.LEFT -> x
        HorizontalAlignment.CENTER -> x + (width/2f)
        HorizontalAlignment.RIGHT -> x + width
    },
    when (verticalAlignment) {
        VerticalAlignment.TOP -> y
        VerticalAlignment.MIDDLE -> y + (height/2f) - text.height/2f
        VerticalAlignment.BOTTOM -> (y + height) - text.height
    },
    horizontalAlignment,
    verticalAlignment,
    style,
    scale
)