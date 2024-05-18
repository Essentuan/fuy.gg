package com.busted_moments.client.framework.render.helpers

import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource

typealias IContext = Context

interface Context {
    val pose: PoseStack
    val buffer: MultiBufferSource
    val partialTicks: Float
    val window: Window
}