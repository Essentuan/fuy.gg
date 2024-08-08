package com.busted_moments.client.framework.keybind

import com.mojang.blaze3d.platform.InputConstants
import com.wynntils.utils.mc.McUtils.mc

typealias Inputs = InputConstants

object Key {
    fun isDown(key: Int): Boolean =
        Inputs.isKeyDown(mc().window.window, key)
}