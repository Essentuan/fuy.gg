@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render.screen.elements

import com.busted_moments.client.framework.render.Element
import com.busted_moments.client.framework.render.Positional
import com.busted_moments.client.framework.render.Renderer
import com.busted_moments.client.framework.render.Renderer.Companion.contains
import com.busted_moments.client.framework.render.Sizable
import com.busted_moments.client.framework.render.screen.Screen
import kotlin.experimental.ExperimentalTypeInference

abstract class HoverElement<T>: Element<Screen.Context>() where T : Positional, T : Sizable {
    override fun draw(ctx: Screen.Context): Boolean =
        true
}

@OverloadResolutionByLambdaReturnType
inline fun <T> T.hover(
    crossinline block: HoverElement<T>.(Screen.Context) -> Boolean
) where T : Renderer<Screen.Context>, T : Positional, T : Sizable {
    if (first)
        this += object : HoverElement<T>() {
            override fun compute(ctx: Screen.Context): Boolean =
                if (this@hover.contains(ctx))
                    block(ctx)
                else
                    false
        }
}

@JvmName("hoverUnit")
inline fun <T> T.hover(
    crossinline block: HoverElement<T>.(Screen.Context) -> Unit
) where T : Renderer<Screen.Context>, T : Positional, T : Sizable {
    if (first)
        this += object : HoverElement<T>() {
            override fun compute(ctx: Screen.Context): Boolean =
                if (this@hover.contains(ctx)) {
                    block(ctx)

                    true
                } else false
        }
}
