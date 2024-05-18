package com.busted_moments.client.framework.render.elements

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Floats

abstract class RectElement<CTX : Context> : Element<CTX>(), Sizable {
    var size: Floats = Floats.ZERO

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