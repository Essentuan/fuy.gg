package com.busted_moments.client.framework.text

import com.wynntils.core.text.PartStyle
import com.wynntils.core.text.StyledTextPart
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.FormattedText
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor
import net.minecraft.util.FormattedCharSequence
import net.minecraft.util.FormattedCharSink
import java.util.Optional
import javax.swing.text.html.HTMLDocument.HTMLReader.CharacterAction

internal const val INHERIT_BIT: Long = 1L
internal const val OBFUSCATED_BIT: Long = (1L shl 1)
internal const val BOLD_BIT: Long = (1L shl 2)
internal const val STRIKETHROUGH_BIT: Long = (1L shl 3)
internal const val UNDERLINE_BIT: Long = (1L shl 4)
internal const val ITALIC_BIT: Long = (1L shl 5)

internal const val MASK = 0xFFFFFFFF
internal const val ALPHA = 255L shl (32 + 24)

data class TextPart(
    var string: String,
    var data: Long,
    var clickEvent: ClickEvent? = null,
    var hoverEvent: HoverEvent? = null
) : FormattedCharSequence {

    constructor(string: String, color: Int) : this(string, color.toLong() shl 32)

    constructor(string: String, color: ChatFormatting) : this(
        string,
        color.color?.or(255 shl 24) ?: throw IllegalArgumentException("${color.name} is not a color!")
    )

    constructor(string: String, style: PartStyle) : this(
        string,
        style.color.asInt()
    ) {
        isInherited = false
        isItalic = style.isItalic
        isBold = style.isBold
        isUnderline = style.isUnderlined
        isStrikethrough = style.isStrikethrough
        isObfuscated = style.isObfuscated

        clickEvent = style.clickEvent
        hoverEvent = style.hoverEvent
    }

    constructor(part: StyledTextPart) : this(part.text, part.style)

    var color: Int
        get() = (data shr 32).toInt()
        set(value) {
            data = value.toLong().shl(32).or(ALPHA) or data.and(MASK)
        }

    val hasColor: Boolean
        get() = (data shr (32 + 24)).and(255) != 0L

    var isInherited: Boolean
        get() = (data and INHERIT_BIT) != 0L
        set(value) {
            data = if (value)
                data or INHERIT_BIT
            else
                data and INHERIT_BIT.inv()
        }

    var isObfuscated: Boolean
        get() = (data and OBFUSCATED_BIT) != 0L
        set(value) {
            data = if (value)
                data or OBFUSCATED_BIT
            else
                data and OBFUSCATED_BIT.inv()
        }

    var isBold: Boolean
        get() = (data and BOLD_BIT) != 0L
        set(value) {
            data = if (value)
                data or BOLD_BIT
            else
                data and BOLD_BIT.inv()
        }

    var isStrikethrough: Boolean
        get() = (data and STRIKETHROUGH_BIT) != 0L
        set(value) {
            data = if (value)
                data or STRIKETHROUGH_BIT
            else
                data and STRIKETHROUGH_BIT.inv()
        }

    var isUnderline: Boolean
        get() = (data and UNDERLINE_BIT) != 0L
        set(value) {
            data = if (value)
                data or UNDERLINE_BIT
            else
                data and UNDERLINE_BIT.inv()
        }

    var isItalic: Boolean
        get() = (data and ITALIC_BIT) != 0L
        set(value) {
            data = if (value)
                data or ITALIC_BIT
            else
                data and ITALIC_BIT.inv()
        }

    fun toStyle(): Style {
        return Style(
            TextColor.fromRgb(color),
            isBold,
            isItalic,
            isUnderline,
            isStrikethrough,
            isObfuscated,
            clickEvent,
            hoverEvent,
            null,
            null
        )
    }

    init {
        isInherited = true
    }

    override fun accept(sink: FormattedCharSink): Boolean {
        val style = if (data == 0L) Style.EMPTY else toStyle()

        var cursor = 0
        while (cursor < string.length) {
            val char = string[cursor]

            when {
                Character.isHighSurrogate(char) -> {
                    if (cursor + 1 < string.length) {
                       val low = string[cursor + 1]

                        if (Character.isLowSurrogate(low)) {
                            if (!sink.accept(cursor, style, Character.toCodePoint(char, low)))
                                return false

                            cursor++
                        }
                    }
                }
                !Character.isLowSurrogate(char) -> {
                    if (!sink.accept(cursor, style, char.code))
                        return false
                }
            }

            cursor++
        }

        return true
    }
}