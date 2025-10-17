package com.busted_moments.client.framework.text

import com.busted_moments.client.framework.FontGlyph
import com.busted_moments.client.framework.FontProvider
import com.busted_moments.client.framework.FontTransposer
import com.busted_moments.client.framework.Fonts
import com.busted_moments.client.framework.wynntils.Ticks
import com.busted_moments.client.framework.render.Split
import com.busted_moments.client.framework.render.TextRenderer
import com.busted_moments.mixin.accessors.PartStyleAccessor
import com.busted_moments.mixin.accessors.StyledTextPartAccessor
import com.wynntils.core.text.PartStyle
import com.wynntils.core.text.StyledText
import com.wynntils.core.text.StyledTextPart
import com.wynntils.core.text.type.StyleType
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.McUtils
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.mc.StyledTextUtils
import net.essentuan.esl.color.Color
import net.essentuan.esl.iteration.extensions.appendAll
import net.essentuan.esl.other.thread
import net.minecraft.ChatFormatting
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.resources.ResourceLocation
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.max
import kotlin.reflect.KProperty


typealias TextParts = List<TextPart>

val FUY_PREFIX: TextParts = Text.parts {
    transpose(Fonts.Pill) {
        +Fonts.Pill.OPEN.color(Color(255, 238, 143))
        +"FUYGG"
        +Fonts.Pill.CLOSE
    }

    +" ⋙ ".reset.white
}

object Text {
    operator fun invoke(): StyledText =
        StyledText.EMPTY

    operator fun invoke(string: String): StyledText =
        StyledText.fromString(string)

    operator fun invoke(string: String, color: CustomColor): StyledText =
        StyledText.fromParts(
            listOf(
                StyledTextPart(
                    string,
                    Style.EMPTY.withColor(color.asInt()),
                    null,
                    null
                )
            )
        )

    operator fun invoke(component: Component): StyledText =
        StyledText.fromComponent(component)

    inline operator fun invoke(block: Builder.() -> Unit): StyledText =
        Builder(mutableListOf()).apply(block).build()

    /**
     * Removes all custom formatting from messages.
     */
    fun normalized(text: StyledText): StyledText {
        return Text(text.replaceAll("[^\\x00-\\x7F§]|[\\n]", "").string.replace("  ", " ")).trim()
    }

    fun strip(string: String): String =
        Text(string).getString(StyleType.NONE)

    fun literal(string: String): StyledText =
        StyledText.fromUnformattedString(string)

    fun component(string: String): Component =
        Text(string).component

    fun component(string: String, style: Style): MutableComponent =
        Component.literal(string).withStyle(style)

    fun component(string: String, color: Int): MutableComponent =
        Component.literal(string).withColor(color)

    fun component(part: TextPart): MutableComponent =
        component(part.string, part.toStyle())

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

    /**
     * Removes the soft-wrap Wynn gives to all messages post 2.1.
     *
     */
    fun StyledText.unwrap(): StyledText =
        StyledTextUtils.unwrap(this)

    inline infix fun StyledText.matches(block: Matching.() -> Unit) =
        Matching(this).apply(block)

    fun TextParts.copy(): TextParts =
        this.mapTo(ArrayList(this.size)) { it.copy() }

    private fun Split.send() {
        for (line in this) {
            if (line.isEmpty())
                continue

            val out = Component.empty()

            for (part in line)
                out.append(component(part.text))

            McUtils.sendMessageToClient(out)
        }
    }

    fun StyledText.send() {
        if (thread().name == "Render thread")
            McUtils.sendMessageToClient(component)
        else
            Ticks.schedule {
                McUtils.sendMessageToClient(component)
            }
    }

    @JvmInline
    value class Matching(
        val text: StyledText
    ) {
        inline operator fun Pattern.invoke(style: StyleType = StyleType.NONE, block: Matcher.(StyledText) -> Unit) {
            val matcher = text.getMatcher(this, style)
            if (matcher.matches())
                block(matcher, text)
        }

        inline fun any(
            vararg patterns: Pattern,
            style: StyleType = StyleType.NONE,
            block: Matcher.(StyledText) -> Unit
        ) {
            for (pattern in patterns) {
                val matcher = text.getMatcher(pattern, style)
                if (matcher.matches()) {
                    block(matcher, text)
                    return
                }
            }
        }

        inline fun unwrapped(block: Matching.() -> Unit) {
            Matching(text.unwrap()).apply(block)
        }

        inline fun replaceAll(pattern: String, replacement: String, block: Matching.() -> Unit) {
            Matching(text.replaceAll(pattern, replacement)).apply(block)
        }

        inline fun trim(block: Matching.() -> Unit) {
            Matching(text.trim()).apply(block)
        }

        inline fun mutate(fixer: (StyledText) -> StyledText, block: Matching.() -> Unit) {
            Matching(fixer(text)).apply(block)
        }
    }

    @JvmInline
    value class Builder(
        val parts: MutableList<TextPart>
    ) {
        fun append(builder: Builder) {
            parts.appendAll(builder.parts)
        }

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
            parts += TextPart(this)
        }

        operator fun FontGlyph.unaryPlus() {
            parts += TextPart(char.toString()).font(font.location)
        }

        /**
         * Appends a \n to the last part if present
         */
        inline fun line(block: Builder.() -> Unit) {
            val before: Int = parts.size

            block()

            if (before != parts.size)
                newLine()
        }

        inline fun center(
            prefix: TextPart,
            maxWidth: Int = ChatComponent.getWidth(mc().options.chatWidth().get()),
            crossinline block: Builder.() -> Unit
        ) = center(
            Text { +prefix },
            maxWidth,
            block
        )

        inline fun center(
            prefix: TextParts,
            maxWidth: Int = ChatComponent.getWidth(mc().options.chatWidth().get()),
            crossinline block: Builder.() -> Unit
        ) = center(
            Text { +prefix },
            maxWidth,
            block
        )

        inline fun center(
            prefix: StyledText,
            maxWidth: Int = ChatComponent.getWidth(mc().options.chatWidth().get()),
            crossinline block: Builder.() -> Unit
        ) = center(
            TextRenderer.split(prefix).width,
            maxWidth,
            block
        )

        inline fun center(
            offset: Float = 0f,
            maxWidth: Int = ChatComponent.getWidth(mc().options.chatWidth().get()),
            crossinline block: Builder.() -> Unit
        ) {
            val builder = Text(block)
            val width = TextRenderer.split(builder).width
            val spaceWidth = TextRenderer.width(' '.code, Style.EMPTY)

            +" ".repeat(((((maxWidth.toFloat() / 2F) - (width / 2F)) / spaceWidth) - offset).toInt().coerceAtLeast(0))
            +builder
        }

        fun newLine() {
            +'\n'.toString().reset
        }

        inline fun transpose(font: FontTransposer, block: Builder.() -> Unit) {
            val start: Int = max(parts.size, 0)

            block()

            val sublist = parts.subList(start, parts.size)
            if (sublist.isEmpty())
                return

            font.transpose(sublist)
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

                    if (part.font == null)
                        part.font = previous.font
                }

                if (
                    part.data == previous?.data &&
                    part.clickEvent == previous.clickEvent &&
                    part.hoverEvent == previous.hoverEvent &&
                    part.font == previous.font
                ) {
                    val styledPart = out.last()
                    out[out.lastIndex] = StyledTextPart(
                        styledPart.text + part.string,
                        styledPart.partStyle.style,
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
            TextPart(this).onClick(action, value)

        fun <T : Any> String.onHover(action: HoverEvent.Action<T>, value: T): TextPart =
            TextPart(this).onHover(action, value)

        fun String.font(font: ResourceLocation): TextPart =
            TextPart(this).font(font)

        fun String.font(font: FontProvider): TextPart =
            TextPart(this).font(font)

        val FontGlyph.black: TextPart
            get() = TextPart(char.toString(), ChatFormatting.BLACK).also { it.font = font.location }
        val FontGlyph.darkBlue: TextPart
            get() = TextPart(char.toString(), ChatFormatting.DARK_BLUE).also { it.font = font.location }
        val FontGlyph.darkGreen: TextPart
            get() = TextPart(char.toString(), ChatFormatting.DARK_GREEN).also { it.font = font.location }
        val FontGlyph.darkAqua: TextPart
            get() = TextPart(char.toString(), ChatFormatting.DARK_AQUA).also { it.font = font.location }
        val FontGlyph.darkRed: TextPart
            get() = TextPart(char.toString(), ChatFormatting.DARK_RED).also { it.font = font.location }
        val FontGlyph.darkPurple: TextPart
            get() = TextPart(char.toString(), ChatFormatting.DARK_PURPLE).also { it.font = font.location }
        val FontGlyph.gold: TextPart
            get() = TextPart(char.toString(), ChatFormatting.GOLD).also { it.font = font.location }
        val FontGlyph.gray: TextPart
            get() = TextPart(char.toString(), ChatFormatting.GRAY).also { it.font = font.location }
        val FontGlyph.darkGray: TextPart
            get() = TextPart(char.toString(), ChatFormatting.DARK_GRAY).also { it.font = font.location }
        val FontGlyph.blue: TextPart
            get() = TextPart(char.toString(), ChatFormatting.BLUE).also { it.font = font.location }
        val FontGlyph.green: TextPart
            get() = TextPart(char.toString(), ChatFormatting.GREEN).also { it.font = font.location }
        val FontGlyph.aqua: TextPart
            get() = TextPart(char.toString(), ChatFormatting.AQUA).also { it.font = font.location }
        val FontGlyph.red: TextPart
            get() = TextPart(char.toString(), ChatFormatting.RED).also { it.font = font.location }
        val FontGlyph.lightPurple: TextPart
            get() = TextPart(char.toString(), ChatFormatting.LIGHT_PURPLE).also { it.font = font.location }
        val FontGlyph.yellow: TextPart
            get() = TextPart(char.toString(), ChatFormatting.YELLOW).also { it.font = font.location }
        val FontGlyph.white: TextPart
            get() = TextPart(char.toString(), ChatFormatting.WHITE).also { it.font = font.location }

        fun FontGlyph.color(color: Color): TextPart =
            TextPart(char.toString(), color.asInt()).also { it.font = font.location }

        val FontGlyph.obfuscate: TextPart
            get() = TextPart(char.toString(), OBFUSCATED_BIT).also { it.font = font.location }
        val FontGlyph.bold: TextPart
            get() = TextPart(char.toString(), BOLD_BIT).also { it.font = font.location }
        val FontGlyph.strikethrough: TextPart
            get() = TextPart(char.toString(), STRIKETHROUGH_BIT).also { it.font = font.location }
        val FontGlyph.underline: TextPart
            get() = TextPart(char.toString(), UNDERLINE_BIT).also { it.font = font.location }
        val FontGlyph.italicize: TextPart
            get() = TextPart(char.toString(), ITALIC_BIT).also { it.font = font.location }

        val FontGlyph.reset: TextPart
            get() = white.apply { isInherited = false; font = this@reset.font.location }

        fun FontGlyph.onClick(action: ClickEvent.Action, value: String) =
            TextPart(char.toString()).onClick(action, value).also { it.font = font.location }

        fun <T : Any> FontGlyph.onHover(action: HoverEvent.Action<T>, value: T): TextPart =
            TextPart(char.toString()).onHover(action, value).also { it.font = font.location }

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

        fun TextPart.font(font: ResourceLocation): TextPart =
            also { it.font = font }

        fun TextPart.font(font: FontProvider): TextPart =
            also { it.font = font.location }
    }
}

@Suppress("CAST_NEVER_SUCCEEDS")
val StyledTextPart.text: String
    get() = (this as StyledTextPartAccessor).text

@Suppress("CAST_NEVER_SUCCEEDS")
val PartStyle.color: CustomColor
    get() = (this as PartStyleAccessor).color

@Suppress("CAST_NEVER_SUCCEEDS")
val PartStyle.clickEvent: ClickEvent
    get() = (this as PartStyleAccessor).clickEvent

@Suppress("CAST_NEVER_SUCCEEDS")
val PartStyle.hoverEvent: HoverEvent
    get() = (this as PartStyleAccessor).hoverEvent

operator fun Matcher.get(group: String): String? = group(group)

operator fun Matcher.get(group: Int): String? = group(group)

operator fun Matcher.getValue(ref: Any?, property: KProperty<*>): String? =
    this[property.name]
