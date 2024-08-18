package com.busted_moments.client.framework.render.screen.elements

import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.keybind.Inputs
import com.busted_moments.client.framework.render.*
import com.busted_moments.client.framework.render.screen.McScreen
import com.busted_moments.client.framework.render.screen.Screen
import com.busted_moments.client.framework.sounds.Sounds.play
import com.busted_moments.client.framework.text.SizedString
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.TextPart
import com.busted_moments.client.framework.text.text
import com.wynntils.core.text.PartStyle
import com.wynntils.core.text.StyledText
import com.wynntils.core.text.StyledTextPart
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.render.buffered.BufferedRenderUtils
import com.wynntils.utils.render.type.TextShadow
import net.essentuan.esl.tuples.numbers.FloatPair
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Style
import net.minecraft.sounds.SoundEvents
import kotlin.experimental.ExperimentalTypeInference

private val SELECTED_STYLE = StyledTextPart(
    "",
    Style.EMPTY.withColor(CommonColors.BLUE.asInt()),
    null,
    null
).partStyle

private const val CURSOR_BLINK = 350.0

abstract class TextInputElement : Screen.Widget(), MutableSizable {
    private var initalized: Boolean = false

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

    var maxWidth: Float = 0f
    var scale: Float = 1f

    private var processed: StyledText = StyledText.EMPTY

    protected abstract val processor: Preprocessor
    protected abstract val inital: SizedString

    var input: SizedString = SizedString.EMPTY
        set(value) {
            field = value
            processed = processor.run { process(value) }

            require(processed.length() == value.length) {
                "Expected '$value\' (${value.length}) but found '${processed.stringWithoutFormatting}' (${processed.length()})"
            }
        }

    var cursor: Int = 0
        get() {
            if (field > input.length)
                field = input.length

            return field
        }
        set(value) {
            if (value < 0)
                return

            field = value.coerceAtMost(input.length)

            selection = when {
                selectionIndex == field -> null
                selectionIndex > field -> field..selectionIndex
                else -> selectionIndex..field
            }
        }

    private var cursorX: Float = 0f

    private var startIndex: Int = 0
        get() {
            if (input.isEmpty())
                return 0

            if (field < 0)
                field = 0

            if (field > input.lastIndex)
                field = input.lastIndex

            return field
        }
        set(value) {
            field = if (input.isEmpty())
                0
            else
                value.coerceIn(input.indices)
        }

    private var endIndex: Int = 0
        get() {
            if (input.isEmpty())
                return 0

            if (field < 0)
                field = 0

            if (field > input.lastIndex)
                field = input.lastIndex

            return field
        }
        set(value) {
            field = if (input.isEmpty())
                0
            else
                value.coerceIn(input.indices)
        }

    private var selectionIndex: Int = 0
        get() {
            if (field > input.length)
                field = input.length

            return field
        }
        set(value) {
            if (value < 0)
                return

            field = value.coerceAtMost(input.length)

            selection = when {
                cursor == field -> null
                cursor > field -> field..cursor
                else -> cursor..field
            }
        }

    var selection: IntRange? = null

    private var selectionStart: Float = 0f
    private var selectionWidth: Float = 0f

    private var rendered: Split = Split()

    private var dragging: Boolean = false

    fun reload() {
        selectionStart = 0f
        selectionWidth = 0f

        var i = -1
        val line = Split.Line()

        for (part in processed) {
            val style = part.partStyle.withBold(false).withItalic(false).withObfuscated(false)

            val characters = mutableListOf<Triple<Char, Float, PartStyle>>()
            var width = 0f

            for (char in part.text) {
                i++

                if (i < startIndex || i > endIndex)
                    continue

                val charWidth = input.widthAt(i)

                if (selection != null && i in selection!!) {
                    if (i == selection!!.first)
                        selectionStart = (line.width + width) * scale

                    selectionWidth += charWidth * scale

                    characters += Triple(char, charWidth, SELECTED_STYLE)
                } else
                    characters += Triple(char, charWidth, style)

                width += charWidth
            }

            if (characters.isNotEmpty()) {
                val builder = StringBuilder()

                var style: PartStyle? = null
                var width = 0f

                for ((char, cWidth, cStyle) in characters) {
                    if (cStyle != style) {
                        if (builder.isNotEmpty()) {
                            line += Split.Computed(
                                TextPart(
                                    builder.toString(),
                                    style!!
                                ),
                                width
                            )

                            builder.clear()
                            width = 0f
                        }

                        style = cStyle
                    }

                    builder.append(char)
                    width += cWidth
                }

                if (builder.isNotEmpty())
                    line += Split.Computed(
                        TextPart(
                            builder.toString(),
                            style!!
                        ),
                        width
                    )
            }
        }

        rendered = Split(mutableListOf(line))
        cursorX = (input.distanceOf(cursor) * scale) - (input.subSequence(0, startIndex).width * scale)
    }

    private fun trimStart() {
        if (input.isEmpty())
            return

        var width = input.subSequence(startIndex, endIndex + 1).width * scale

        if (width < (maxWidth - 3)) {
            if (startIndex == 0)
                return

            for (i in (startIndex - 1) downTo 0) {
                width += input.widthAt(i) * scale

                if (width > (maxWidth - 3)) {
                    startIndex = i - 1
                    reload()

                    return
                }
            }

            startIndex = 0
            reload()
        } else {
            for (i in startIndex..<input.length) {
                width -= input.widthAt(i) * scale

                if (width < (maxWidth - 3)) {
                    startIndex = i
                    reload()

                    return
                }
            }
        }
    }

    private fun trimEnd() {
        if (input.isEmpty())
            return

        var width = input.subSequence(startIndex, endIndex + 1).width * scale

        if (width < (maxWidth - 3)) {
            if (endIndex == input.lastIndex)
                return

            for (i in endIndex..<input.length) {
                width += input.widthAt(i) * scale

                if (width > (maxWidth - 3)) {
                    endIndex = i - 1
                    reload()

                    return
                }
            }

            endIndex = input.lastIndex
            reload()
        } else {
            for (i in endIndex downTo 0) {
                width -= input.widthAt(i) * scale

                if (width < (maxWidth - 3)) {
                    endIndex = i
                    reload()

                    return
                }
            }
        }
    }

    private fun insert(string: String) {
        if (selection != null) {
            input = input.subSequence(0, selection!!.first) + string +
                    if (selection!!.last >= input.lastIndex)
                        ""
                    else
                        input.subSequence(selection!!.last + 1, input.length)

            cursor = selection!!.first
            selectionIndex = cursor

            trimStart()
        } else {
            input = input.subSequence(0, cursor) + string +
                    if (cursor > input.lastIndex)
                        ""
                    else
                        input.subSequence(cursor, input.length)

            cursor++
            selectionIndex = cursor
            endIndex++

            trimStart()
        }

        reload()
    }

    private fun indexAt(mouseX: Float): Int {
        return input.indexAt(input.subSequence(0, startIndex).width + ((mouseX - x + 3f) * (1f / scale)))
    }

    override fun charTyped(codePoint: Char, modifiers: Int): Boolean {
        if (!isFocused)
            return false

        insert(codePoint.toString())

        return true
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean =
        when {
            !isFocused -> false
            McScreen.isCopy(keyCode) -> {
                mc().keyboardHandler.clipboard = if (selection != null)
                    input.subSequence(selection!!).toString()
                else
                    input.toString()

                true
            }

            McScreen.isPaste(keyCode) -> {
                insert(mc().keyboardHandler.clipboard)

                reload()

                true
            }

            McScreen.isCut(keyCode) -> {
                if (selection != null) {
                    mc().keyboardHandler.clipboard = input.subSequence(selection!!).toString()
                    insert("")
                } else {
                    mc().keyboardHandler.clipboard = input.toString()
                    input = SizedString.EMPTY
                }

                reload()

                true
            }

            McScreen.isSelectAll(keyCode) -> {
                cursor = 0
                selectionIndex = input.lastIndex

                reload()

                true
            }

            keyCode == Inputs.KEY_BACKSPACE || keyCode == Inputs.KEY_DELETE -> {
                if (selection != null) {
                    insert("")

                    true
                } else if (cursor > 0 && input.isNotEmpty()) {
                    input = input.subSequence(0, cursor - 1) + input.subSequence(cursor, input.length)

                    trimStart()

                    reload()

                    true
                } else false
            }

            keyCode == Inputs.KEY_LEFT -> {
                if (McScreen.hasShiftDown()) {
                    if (selection == null)
                        selectionIndex = cursor
                } else
                    selectionIndex = (cursor - 1)

                cursor--

                if (startIndex > cursor) {
                    startIndex--

                    trimEnd()
                }

                reload()

                true
            }

            keyCode == Inputs.KEY_RIGHT -> {
                if (McScreen.hasShiftDown()) {
                    if (selection == null)
                        selectionIndex = cursor
                } else
                    selectionIndex = (cursor + 1)

                cursor++

                if ((endIndex + 1) < cursor) {
                    endIndex++

                    trimStart()
                }

                reload()

                true
            }

            else -> false
        }

    override fun isMouseOver(mouseX: Double, mouseY: Double): Boolean {
        return (mouseX.toFloat() - x) < maxWidth && super.isMouseOver(mouseX, mouseY)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (((mouseX.toFloat() - x) > maxWidth))
            return false

        if (!isMouseOver(mouseX, mouseY)) {
            if (isFocused) {
                SoundEvents.UI_BUTTON_CLICK.play()
                isFocused = false
            }

            return false
        }

        SoundEvents.UI_BUTTON_CLICK.play()

        when (button) {
            Inputs.MOUSE_BUTTON_LEFT -> {
                val before = cursor

                cursor = indexAt(mouseX.toFloat()).coerceIn(startIndex..(endIndex + 1))
                selectionIndex = cursor

                when {
                    before == cursor && before == startIndex -> {
                        startIndex -= 5

                        trimEnd()

                        cursor = startIndex
                        selectionIndex = cursor
                    }

                    before == cursor && before > endIndex -> {
                        endIndex += 5

                        trimStart()

                        cursor = endIndex + 1
                        selectionIndex = cursor
                    }
                }

                reload()

                dragging = true
            }

            Inputs.MOUSE_BUTTON_RIGHT -> {
                cursor = 0

                startIndex = 0
                endIndex = 0

                input = SizedString.EMPTY

                reload()
            }
        }

        return true
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!dragging)
            return false

        val before = selection

        selectionIndex = indexAt(mouseX.toFloat())

        if (before != selection)
            reload()

        return true
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        dragging = false

        return false
    }

    private val cursorActive: Boolean
        get() =
            ((System.currentTimeMillis() % (CURSOR_BLINK * 2.0)) / (CURSOR_BLINK * 2.0)) < 0.5

    override fun renderWidget(ctx: Screen.Context): Boolean {
        if (!initalized) {
            input = inital
            endIndex = input.lastIndex
            cursor = input.length
            selectionIndex = cursor

            trimEnd()
            reload()
            initalized = true
        }

        x = this@TextInputElement.x
        y = this@TextInputElement.y

        BufferedRenderUtils.drawRect(
            ctx.pose,
            ctx.buffer,
            CommonColors.BLACK,
            x,
            y,
            0f,
            width,
            height
        )

        BufferedRenderUtils.drawRectBorders(
            ctx.pose,
            ctx.buffer,
            CommonColors.LIGHT_GRAY,
            x,
            y,
            x + width,
            y + height,
            0f,
            1f
        )

        val textX = x + 3f
        val textY = (y + height / 2f - ((TextRenderer.font.lineHeight / 2f) * scale)) + 1f

        if (selection != null) {
            BufferedRenderUtils.drawRect(
                ctx.pose,
                ctx.buffer,
                CommonColors.WHITE,
                textX + selectionStart,
                textY - 1.5f,
                0f,
                selectionWidth,
                TextRenderer.font.lineHeight.toFloat()
            )
        }

        ctx.text(
            rendered,
            textX,
            textY,
            scale = scale,
            style = TextShadow.NONE
        )

        if (selection == null && cursorActive && isFocused)
            ctx.line(
                CommonColors.WHITE.esl,
                textX + cursorX,
                textY - 1f,
                textX + cursorX,
                textY + (TextRenderer.font).lineHeight * scale,
                0.5f
            )

        return true
    }

    /**
     * A preprocessor for adding color to a text input
     *
     * Note: Does not support [ChatFormatting.BOLD], [ChatFormatting.ITALIC], or [ChatFormatting.OBFUSCATED]
     */
    fun interface Preprocessor {
        fun TextInputElement.process(input: SizedString): StyledText

        companion object : Preprocessor {
            override fun TextInputElement.process(input: SizedString): StyledText =
                Text.literal(input.toString())
        }
    }
}

@OverloadResolutionByLambdaReturnType
@OptIn(ExperimentalTypeInference::class)
inline fun Renderer<Screen.Context>.textinput(
    input: SizedString = SizedString.EMPTY,
    preprocessor: TextInputElement.Preprocessor = TextInputElement.Preprocessor,
    crossinline block: TextInputElement.(Screen.Context) -> Boolean
) {
    if (first)
        this += object : TextInputElement() {
            override val processor: Preprocessor
                get() = preprocessor
            override val inital: SizedString
                get() = input

            override fun compute(ctx: Screen.Context): Boolean = block(ctx)
        }
}

@JvmName("textInputUnit")
inline fun Renderer<Screen.Context>.textinput(
    input: SizedString = SizedString.EMPTY,
    preprocessor: TextInputElement.Preprocessor = TextInputElement.Preprocessor,
    crossinline block: TextInputElement.(Screen.Context) -> Unit
) {
    if (first)
        this += object : TextInputElement() {
            override val processor: Preprocessor
                get() = preprocessor

            override val inital: SizedString
                get() = input

            override fun compute(ctx: Screen.Context): Boolean {
                block(ctx)

                return true
            }
        }
}