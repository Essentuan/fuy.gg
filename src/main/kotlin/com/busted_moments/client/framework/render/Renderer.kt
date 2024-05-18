package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Floats
import com.busted_moments.client.framework.render.helpers.Percentage
import com.mojang.blaze3d.platform.Window

//Having children be initialized on first render
//is well... a bit stupid
//But it was fun to write!
interface Renderer<CTX: Context> {
    val first: Boolean

    /**
     * @return `true` if this element was rendered
     */
    fun render(ctx: CTX): Boolean

    operator fun plusAssign(child: Element<out CTX>)

    operator fun iterator(): Iterator<Element<out CTX>>
}

infix fun Percentage.of(sizeable: Window): Floats = Floats(
    sizeable.width * factor,
    sizeable.height * factor
)

operator fun Percentage.plus(sizable: Window): Floats = Floats(
    sizable.width / factor,
    sizable.height / factor
)

operator fun Window.plus(pct: Percentage): Floats = Floats(
    width / pct.factor,
    height / pct.factor
)