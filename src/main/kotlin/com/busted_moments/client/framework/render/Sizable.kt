package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.helpers.Percentage
import net.essentuan.esl.tuples.numbers.FloatPair

interface Sizable {
    val width: Float
    val height: Float
}

infix fun Percentage.of(sizeable: Sizable): FloatPair = FloatPair(
    sizeable.width * factor,
    sizeable.height * factor
)

operator fun Percentage.plus(sizable: Sizable): FloatPair = FloatPair(
    sizable.width / factor,
    sizable.height / factor
)

operator fun Sizable.plus(pct: Percentage): FloatPair = FloatPair(
    width / pct.factor,
    height / pct.factor
)