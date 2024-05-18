package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.helpers.Floats
import com.busted_moments.client.framework.render.helpers.Percentage

interface Sizable {
    val width: Float
    val height: Float
}

infix fun Percentage.of(sizeable: Sizable): Floats = Floats(
    sizeable.width * factor,
    sizeable.height * factor
)

operator fun Percentage.plus(sizable: Sizable): Floats = Floats(
    sizable.width / factor,
    sizable.height / factor
)

operator fun Sizable.plus(pct: Percentage): Floats = Floats(
    width / pct.factor,
    height / pct.factor
)