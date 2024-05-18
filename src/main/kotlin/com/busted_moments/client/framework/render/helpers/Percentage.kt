package com.busted_moments.client.framework.render.helpers

@JvmInline
value class Percentage private constructor(val factor: Float) {
    infix fun of(number: Number): Float =
        number.toFloat() * factor

    infix fun of(floats: Floats): Floats = floats.copy(
        floats.first * factor,
        floats.second * factor
    )

    operator fun plus(number: Number): Float =
        number.toFloat() / factor

    operator fun plus(floats: Floats): Floats = floats.copy(
        floats.first / factor,
        floats.second / factor
    )

    companion object {
        val Number.pct: Percentage
            get() = Percentage(toFloat() / 100f)

        operator fun Number.plus(pct: Percentage): Float =
            toFloat() / pct.factor

        operator fun Floats.plus(pct: Percentage): Floats = copy(
            first / pct.factor,
            second / pct.factor
        )
    }
}