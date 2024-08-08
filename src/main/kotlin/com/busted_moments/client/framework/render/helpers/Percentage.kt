package com.busted_moments.client.framework.render.helpers

import com.busted_moments.client.framework.config.Config.copy
import net.essentuan.esl.tuples.numbers.FloatPair

@JvmInline
value class Percentage private constructor(val factor: Float) {
    infix fun of(number: Int): Float =
        number.toFloat() * factor

    infix fun of(number: Long): Float =
        number.toFloat() * factor

    infix fun of(number: Float): Float =
        number * factor

    infix fun of(number: Double): Float =
        number.toFloat() * factor

    infix fun of(floats: FloatPair): FloatPair = floats.copy(
        floats.first * factor,
        floats.second * factor
    )

    operator fun plus(number: Int): Float =
        number.toFloat() / factor

    operator fun plus(number: Long): Float =
        number.toFloat() / factor

    operator fun plus(number: Float): Float =
        number / factor

    operator fun plus(number: Double): Float =
        number.toFloat() / factor

    operator fun plus(floats: FloatPair): FloatPair = floats.copy(
        floats.first / factor,
        floats.second / factor
    )

    companion object {
        val Int.pct: Percentage
            get() = Percentage(toFloat() / 100f)

        val Long.pct: Percentage
            get() = Percentage(toFloat() / 100f)

        val Float.pct: Percentage
            get() = Percentage(this / 100f)

        val Double.pct: Percentage
            get() = Percentage(toFloat() / 100f)

        operator fun Int.plus(pct: Percentage): Float =
            toFloat() / pct.factor

        operator fun Long.plus(pct: Percentage): Float =
            toFloat() / pct.factor

        operator fun Float.plus(pct: Percentage): Float =
            toFloat() / pct.factor

        operator fun Double.plus(pct: Percentage): Float =
            toFloat() / pct.factor

        operator fun FloatPair.plus(pct: Percentage): FloatPair = copy(
            first / pct.factor,
            second / pct.factor
        )
    }
}