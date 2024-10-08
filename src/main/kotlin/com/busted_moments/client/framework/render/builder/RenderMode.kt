package com.busted_moments.client.framework.render.builder

import com.mojang.blaze3d.systems.RenderSystem

sealed class RenderMode(val id: Int) {
    abstract fun enable()

    abstract fun disable()

    companion object {
        val DISABLE_DEPTH: RenderMode
            get() = DisableDepth
        val DISABLE_CULL: RenderMode
            get() = DisableCull
        val BLEND: RenderMode
            get() = Blend

        const val TOTAL = 4

        fun scissor(x: Int, y: Int, width: Int, height: Int): RenderMode =
            Scissor(x, y, width, height)
    }

    private data object DisableDepth : RenderMode(0) {
        override fun enable() {
            RenderSystem.disableDepthTest()
        }

        override fun disable() {
            RenderSystem.enableDepthTest()
        }
    }

    private data class Scissor(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int
    ) : RenderMode(1) {
        override fun enable() {
            RenderSystem.enableScissor(x, y, width, height)
        }

        override fun disable() {
            RenderSystem.disableScissor()
        }
    }

    private data object DisableCull : RenderMode(2) {
        override fun enable() {
            RenderSystem.disableCull()
        }

        override fun disable() {
            RenderSystem.enableCull()
        }
    }

    private data object Blend : RenderMode(3) {
        override fun enable() {
            RenderSystem.enableBlend()
        }

        override fun disable() {
            RenderSystem.disableBlend()
        }
    }
}