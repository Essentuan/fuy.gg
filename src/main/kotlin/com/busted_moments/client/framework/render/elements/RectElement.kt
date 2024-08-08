package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.MutableSizable
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.helpers.Context
import net.essentuan.esl.tuples.numbers.FloatPair

abstract class RectElement<CTX : Context> : Element<CTX>(), MutableSizable {
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
}