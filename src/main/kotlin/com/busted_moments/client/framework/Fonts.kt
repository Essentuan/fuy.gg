package com.busted_moments.client.framework

import com.busted_moments.client.framework.text.TextPart
import net.minecraft.resources.ResourceLocation

object Fonts {
    object Default : FontProvider("minecraft:default")
    object Pill : FontTransposer(
        "minecraft:banner/pill",
        Strategy.offset('A'..'Z', 57392 - 'A'.code, true),
        Strategy.mapped(57418, '?'),
        Strategy.mapped(56419, '['),
        Strategy.mapped(56420, ']'),
        Strategy.mapped(56421, '/'),
        Strategy.mapped(56422, '%'),
        Strategy.mapped(56423, '&'),
        Strategy.offset('0'..'9', 57424 - '9'.code),
        Strategy.mapped(57434, '!'),
        Strategy.mapped(57435, '('),
        Strategy.mapped(57436, ')'),
        Strategy.mapped(57437, '<'),
        Strategy.mapped(57438, '='),
        Strategy.mapped(57439, '>'),
        Strategy.mapped(57441, ' ')
    ) {
        val OPEN = glyph(57440)
        val CLOSE = glyph(57442)
        val SEPARATOR_LEFT = glyph('\uDAFF')
        val SEPARATOR_RIGHT = glyph('\uDFFF')

        override fun transpose(parts: List<TextPart>) {
            val builder = StringBuilder()

            for (part in parts) {
                for (char in part.string) {
                    val c = transpose(char)

                    builder.append(c)

                    if (c != SEPARATOR_LEFT.char && c != SEPARATOR_RIGHT.char) {
                        if (c == CLOSE.char)
                            builder.append(SEPARATOR_LEFT.char)
                        else {
                            builder.append(SEPARATOR_LEFT.char)
                            builder.append(SEPARATOR_RIGHT.char)
                        }
                    }
                }

                part.string = builder.toString()
                builder.clear()
            }
        }
    }
}

open class FontProvider(
    val location: ResourceLocation
) {
    constructor(string: String) : this(ResourceLocation.parse(string))

    constructor(namespace: String, path: String) : this(ResourceLocation.fromNamespaceAndPath(namespace, path))
}

data class FontGlyph(
    val font: FontProvider,
    val char: Char
)

abstract class FontTransposer(
    location: ResourceLocation,
    vararg strategies: Strategy
) : FontProvider(location) {
    private val chars: MutableMap<Int, Char> = mutableMapOf()

    constructor(
        string: String,
        vararg strategies: Strategy
    ) : this(
        ResourceLocation.parse(string),
        *strategies
    )

    constructor(
        namespace: String,
        path: String,
        vararg strategies: Strategy
    ) : this(
        ResourceLocation.fromNamespaceAndPath(namespace, path),
        *strategies
    )

    init {
        for (strategy in strategies)
            strategy.apply(chars)
    }

    open fun transpose(parts: List<TextPart>) {
        for (part in parts) {
            if (part.font == null) {
                part.string = transpose(part.string)
                part.font = location
            }
        }
    }

    open fun transpose(string: String): String {
        val chars = string.toCharArray()
        val out = CharArray(chars.size)

        for (i in chars.indices)
            out[i] = transpose(chars[i])

        return String(out)
    }

    open fun transpose(char: Char): Char =
        chars[char.code] ?: char

    fun glyph(char: Char) =
        FontGlyph(this, char)

    fun glyph(char: Int) =
        FontGlyph(this, char.toChar())
}

fun interface Strategy {
    fun apply(chars: MutableMap<Int, Char>)

    companion object {
        fun mapped(char: Char, to: Char): Strategy =
            Strategy { it[to.code] = char }

        fun mapped(char: Int, to: Char): Strategy =
            Strategy { it[to.code] = char.toChar() }

        fun offset(chars: CharRange, offset: Int, ignoreCase: Boolean = false) =
            Strategy {
                for (c in chars) {
                    if (ignoreCase) {
                        it[c.uppercaseChar().code] = (c.code + offset).toChar()
                        it[c.lowercaseChar().code] = (c.code + offset).toChar()
                    } else
                        it[c.code] = (c.code + offset).toChar()
                }
            }
    }
}