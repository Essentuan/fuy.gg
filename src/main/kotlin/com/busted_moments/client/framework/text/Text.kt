package com.busted_moments.client.framework.text

import com.busted_moments.mixin.accessors.PartStyleAccessor
import com.busted_moments.mixin.accessors.StyledTextPartAccessor
import com.wynntils.core.text.PartStyle
import com.wynntils.core.text.StyledText
import com.wynntils.core.text.StyledTextPart
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.TextColor

typealias StyleType = PartStyle.StyleType
typealias TextParts = List<TextPart>

val FUY_PREFIX: TextParts = Text.parts {
    +"[".darkGreen
    +"f".red
    +"u".gold
    +"y".yellow
    +".".green
    +"g".aqua
    +"g".blue
    +"]".darkGreen
    +" ⋙ ".white
}

object Text {
    operator fun invoke(): StyledText =
        StyledText.EMPTY

    operator fun invoke(string: String): StyledText =
        StyledText.fromString(string)

    operator fun invoke(component: Component): StyledText =
        StyledText.fromComponent(component)

    inline operator fun invoke(block: Builder.() -> Unit): StyledText =
        Builder(mutableListOf()).apply(block).build()

    fun literal(string: String): StyledText =
        StyledText.fromUnformattedString(string)

    fun component(string: String): Component =
        Text(string).component

    fun component(block: Builder.() -> Unit): Component =
        Text(block).component

    inline operator fun StyledText.invoke(crossinline block: Builder.() -> Unit): StyledText = Text {
        +this@invoke

        block()
    }

    inline operator fun TextParts.invoke(block: Builder.() -> Unit): StyledText =
        Builder(this.mapTo(ArrayList(this.size)) { it.copy() }).apply(block).build()

    inline fun parts(crossinline block: Builder.() -> Unit): TextParts {
        val builder = Builder(mutableListOf())
        builder.block()

        return builder.parts.toList()
    }

    fun TextParts.copy(): TextParts =
        this.mapTo(ArrayList(this.size)) { it.copy() }

    class Builder(
        val parts: MutableList<TextPart>
    ) {
        private inline fun append(builder: Builder) {
            parts += builder.parts
        }

        operator fun Builder.unaryPlus() =
            append(this)

        operator fun TextParts.unaryPlus() {
            parts += this
        }

        operator fun StyledText.unaryPlus() {
            for (part in this)
                parts += TextPart(part)
        }

        operator fun Component.unaryPlus() {
            +Text(this)
        }

        operator fun TextPart.unaryPlus() {
            parts += this
        }

        operator fun String.unaryPlus() {
            parts += TextPart(this, 0)
        }

        inline fun line(block: Builder.() -> Unit) {
            val before: Int = parts.size

            block()

            if (before != parts.size)
                newLine()
        }

        /**
         * Appends a \n to the last part if present
         *
         */
        fun newLine() {
            (parts.lastOrNull() ?: return).string += '\n'
        }

        fun build(): StyledText {
            val out: MutableList<StyledTextPart> = ArrayList(parts.size)

            for (i in parts.indices) {
                val part = parts[i]
                val previous = parts.getOrNull(i - 1)

                if (previous == null && !part.hasColor)
                    part.color = 0xFFFFFF

                if (part.isInherited && previous != null) {
                    if (!part.hasColor)
                        part.color = previous.color

                    if (!part.isBold)
                        part.isBold = previous.isBold

                    if (!part.isItalic)
                        part.isItalic = previous.isItalic

                    if (!part.isUnderline)
                        part.isUnderline = previous.isUnderline

                    if (!part.isObfuscated)
                        part.isObfuscated = previous.isObfuscated

                    if (!part.isStrikethrough)
                        part.isStrikethrough = previous.isStrikethrough

                    if (part.clickEvent == null)
                        part.clickEvent = previous.clickEvent

                    if (part.hoverEvent == null)
                        part.hoverEvent = previous.hoverEvent
                }

                if (
                    part.data == previous?.data &&
                    part.clickEvent == previous.clickEvent &&
                    part.hoverEvent == previous.hoverEvent
                ) {
                    val styledPart = out.last()
                    out[out.lastIndex] = StyledTextPart(
                        styledPart.text + part.string,
                        styledPart.style.style,
                        null,
                        null
                    )
                } else
                    out += StyledTextPart(
                        part.string,
                        part.toStyle(),
                        null,
                        null
                    )
            }

            return StyledText.fromParts(out)
        }

        val String.black: TextPart
            get() = TextPart(this, ChatFormatting.BLACK)
        val String.darkBlue: TextPart
            get() = TextPart(this, ChatFormatting.DARK_BLUE)
        val String.darkGreen: TextPart
            get() = TextPart(this, ChatFormatting.DARK_GREEN)
        val String.darkAqua: TextPart
            get() = TextPart(this, ChatFormatting.DARK_AQUA)
        val String.darkRed: TextPart
            get() = TextPart(this, ChatFormatting.DARK_RED)
        val String.darkPurple: TextPart
            get() = TextPart(this, ChatFormatting.DARK_PURPLE)
        val String.gold: TextPart
            get() = TextPart(this, ChatFormatting.GOLD)
        val String.gray: TextPart
            get() = TextPart(this, ChatFormatting.GRAY)
        val String.darkGray: TextPart
            get() = TextPart(this, ChatFormatting.DARK_GRAY)
        val String.blue: TextPart
            get() = TextPart(this, ChatFormatting.BLUE)
        val String.green: TextPart
            get() = TextPart(this, ChatFormatting.GREEN)
        val String.aqua: TextPart
            get() = TextPart(this, ChatFormatting.AQUA)
        val String.red: TextPart
            get() = TextPart(this, ChatFormatting.RED)
        val String.lightPurple: TextPart
            get() = TextPart(this, ChatFormatting.LIGHT_PURPLE)
        val String.yellow: TextPart
            get() = TextPart(this, ChatFormatting.YELLOW)
        val String.white: TextPart
            get() = TextPart(this, ChatFormatting.WHITE)

        fun String.color(color: Color): TextPart =
            TextPart(this, color.asInt())

        val String.obfuscate: TextPart
            get() = TextPart(this, OBFUSCATED_BIT)
        val String.bold: TextPart
            get() = TextPart(this, BOLD_BIT)
        val String.strikethrough: TextPart
            get() = TextPart(this, STRIKETHROUGH_BIT)
        val String.underline: TextPart
            get() = TextPart(this, UNDERLINE_BIT)
        val String.italicize: TextPart
            get() = TextPart(this, ITALIC_BIT)

        val String.reset: TextPart
            get() = white.apply { isInherited = false }

        fun String.onClick(action: ClickEvent.Action, value: String) =
            TextPart(this, 0).onClick(action, value)

        fun <T : Any> String.onHover(action: HoverEvent.Action<T>, value: T): TextPart =
            TextPart(this, 0).onHover(action, value)

        val TextPart.black: TextPart
            get() = apply { this.color = ChatFormatting.BLACK.color!! }
        val TextPart.darkBlue: TextPart
            get() = apply { this.color = ChatFormatting.DARK_BLUE.color!! }
        val TextPart.darkGreen: TextPart
            get() = apply { this.color = ChatFormatting.DARK_GREEN.color!! }
        val TextPart.darkAqua: TextPart
            get() = apply { this.color = ChatFormatting.DARK_AQUA.color!! }
        val TextPart.darkRed: TextPart
            get() = apply { this.color = ChatFormatting.DARK_RED.color!! }
        val TextPart.darkPurple: TextPart
            get() = apply { this.color = ChatFormatting.DARK_PURPLE.color!! }
        val TextPart.gold: TextPart
            get() = apply { this.color = ChatFormatting.GOLD.color!! }
        val TextPart.gray: TextPart
            get() = apply { this.color = ChatFormatting.GRAY.color!! }
        val TextPart.darkGray: TextPart
            get() = apply { this.color = ChatFormatting.DARK_GRAY.color!! }
        val TextPart.blue: TextPart
            get() = apply { this.color = ChatFormatting.BLUE.color!! }
        val TextPart.green: TextPart
            get() = apply { this.color = ChatFormatting.GREEN.color!! }
        val TextPart.aqua: TextPart
            get() = apply { this.color = ChatFormatting.AQUA.color!! }
        val TextPart.red: TextPart
            get() = apply { this.color = ChatFormatting.RED.color!! }
        val TextPart.lightPurple: TextPart
            get() = apply { this.color = ChatFormatting.LIGHT_PURPLE.color!! }
        val TextPart.yellow: TextPart
            get() = apply { this.color = ChatFormatting.YELLOW.color!! }
        val TextPart.white: TextPart
            get() = apply { this.color = ChatFormatting.WHITE.color!! }

        fun TextPart.color(color: Color) =
                apply { this.color = color.asInt() }

        val TextPart.obfuscate: TextPart
            get() = apply { this.isObfuscated = true }
        val TextPart.bold: TextPart
            get() = apply { this.isBold = true }
        val TextPart.strikethrough: TextPart
            get() = apply { this.isStrikethrough = true }
        val TextPart.underline: TextPart
            get() = apply { this.isUnderline = true }
        val TextPart.italicize: TextPart
            get() = apply { this.isItalic = true }

        val TextPart.reset: TextPart
            get() = apply {
                data = ChatFormatting.WHITE.color!!.toLong() shl 32
            }

        fun TextPart.onClick(action: ClickEvent.Action, value: String) =
            also { clickEvent = ClickEvent(action, value) }

        fun <T : Any> TextPart.onHover(action: HoverEvent.Action<T>, value: T): TextPart =
            also { hoverEvent = HoverEvent(action, value) }
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
val StyledTextPart.text: String
    get() = (this as StyledTextPartAccessor).text

@Suppress("CAST_NEVER_SUCCEEDS")
val StyledTextPart.style: PartStyle
    get() = (this as StyledTextPartAccessor).style

@Suppress("CAST_NEVER_SUCCEEDS")
val PartStyle.color: CustomColor
    get() = (this as PartStyleAccessor).color

@Suppress("CAST_NEVER_SUCCEEDS")
val PartStyle.clickEvent: ClickEvent
    get() = (this as PartStyleAccessor).clickEvent

@Suppress("CAST_NEVER_SUCCEEDS")
val PartStyle.hoverEvent: HoverEvent
    get() = (this as PartStyleAccessor).hoverEvent