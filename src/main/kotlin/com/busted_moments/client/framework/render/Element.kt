@file:OptIn(ExperimentalTypeInference::class)

package com.busted_moments.client.framework.render

import com.busted_moments.client.framework.render.helpers.Context
import com.busted_moments.client.framework.render.helpers.Floats
import com.mojang.blaze3d.platform.InputConstants
import kotlin.experimental.ExperimentalTypeInference

typealias Keys = InputConstants

abstract class Element<CTX : Context> : Renderer<CTX>, Positional {
    override var first: Boolean = true
        protected set
    var pos: Floats = Floats.ZERO

    private var children = mutableListOf<Element<out CTX>>()

    override var x: Float
        get() = pos.first
        set(value) {
            pos = pos.copy(first = value)
        }

    override var y: Float
        get() = pos.second
        set(value) {
            pos = pos.copy(second = value)
        }

    protected open fun pre(ctx: CTX) = Unit

    protected open fun post(ctx: CTX) = Unit

    protected abstract fun compute(ctx: CTX): Boolean

    protected abstract fun draw(ctx: CTX): Boolean

    final override fun render(ctx: CTX): Boolean {
        val out = compute(ctx) && run {
            pre(ctx)
            draw(ctx)
        }

        if (out)
            @Suppress("UNCHECKED_CAST")
            for (child in this)
                (child as Renderer<CTX>).render(ctx)

        post(ctx)

        first = children.isEmpty()

        return out
    }

    override fun plusAssign(child: Element<out CTX>) {
        children += child
    }

    override fun iterator(): Iterator<Element<out CTX>> =
        children.iterator()

    abstract class Dynamic<CTX : Context> : Element<CTX>() {
        override fun compute(ctx: CTX): Boolean = true
    }
}


@OverloadResolutionByLambdaReturnType
inline fun <CTX : Context> Renderer<CTX>.dynamic(
    crossinline block: Element.Dynamic<CTX>.(CTX) -> Boolean
) {
    if (first)
        this += object : Element.Dynamic<CTX>() {
            override fun draw(ctx: CTX): Boolean = block(ctx)
        }
}

@JvmName("dynamicUnit")
inline fun <CTX : Context> Renderer<CTX>.dynamic(
    crossinline block: Element.Dynamic<CTX>.(CTX) -> Unit
) {
    if (first)
        this += object : Element.Dynamic<CTX>() {
            override fun draw(ctx: CTX): Boolean {
                block(ctx)

                return true
            }
        }
}