package com.busted_moments.client.framework.render.helpers

import com.mojang.blaze3d.platform.Window
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.DeltaTracker
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.MultiBufferSource

typealias IContext = Context

interface Context {
    val graphics: GuiGraphics
    val pose: PoseStack
    val buffer: MultiBufferSource.BufferSource
    val deltaTracker: DeltaTracker
    val window: Window
}
