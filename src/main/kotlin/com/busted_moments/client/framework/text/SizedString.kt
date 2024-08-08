package com.busted_moments.client.framework.text

import com.busted_moments.client.framework.render.TextRenderer
import net.essentuan.esl.iteration.extensions.map
import net.minecraft.network.chat.Style

private const val CHAR_MASK: Long = 0xFFFFFFFF

private fun sizeChar(char: Char, width: Float = TextRenderer.width(char.code, Style.EMPTY)): Long =
    (char.code.toLong() shl 32) or (width.toRawBits().toLong() and CHAR_MASK)

private val Long.char: Char
    get() = (this ushr 32).toInt().toChar()

private val Long.width: Float
    get() =
        Float.fromBits((this and CHAR_MASK).toInt())

class SizedString private constructor(
    private val chars: LongArray
) : CharSequence, Iterable<Pair<Char, Float>> {
    val width: Float = chars.sumOf { it.width.toDouble() }.toFloat()

    constructor(string: CharSequence) : this(
        LongArray(string.length) { i -> sizeChar(string[i]) }
    )

    override val length: Int
        get() = chars.size

    override fun get(index: Int): Char =
        chars[index].char

    fun widthAt(index: Int) =
        chars[index].width

    operator fun plus(char: Char): SizedString =
        SizedString(chars + sizeChar(char))

    operator fun plus(string: SizedString) =
        SizedString(chars + string.chars)

    operator fun plus(string: CharSequence) =
        if (string is SizedString)
            SizedString(chars + string.chars)
        else
            SizedString(chars + LongArray(string.length) { i -> sizeChar(string[i]) })

    override fun subSequence(startIndex: Int, endIndex: Int): SizedString =
        SizedString(chars.copyOfRange(startIndex, endIndex))

    /**
     * Index of character at [x]
     *
     * Note: This method may return [length]
     */
    fun indexAt(x: Float): Int {
        return when {
            isEmpty() -> 0
            x < 0 -> 0
            x > width + 3 -> length
            else -> {
                var width = 0f
                for (i in indices) {
                    val sized = chars[i]

                    when {
                        width + sized.width > x -> return i - 1
                        else -> width += sized.width
                    }
                }

                lastIndex
            }
        }
    }

    /**
     * @return The distance from the start to [i]
     */
    fun distanceOf(i: Int): Float = when {
        i >= length -> width
        i < 0 -> 0f
        else -> (0..<i).fold(0f) { result, it -> result + chars[it].width }
    }

    override fun equals(other: Any?): Boolean {
        if (other !is SizedString)
            return false

        return this === other || other contentEquals this
    }

    override fun hashCode(): Int =
        chars.fold(0) { result, it ->
            31 * result + it.char.code
        }

    override fun iterator(): Iterator<Pair<Char, Float>> =
        chars.iterator().map { it.char to it.width }

    override fun toString(): String =
        String(CharArray(chars.size) { chars[it].char })

    companion object {
        val EMPTY = SizedString(LongArray(0))
    }
}