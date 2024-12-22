package com.busted_moments.client.framework.util

import net.minecraft.client.Minecraft
import net.minecraft.client.User
import net.minecraft.client.player.LocalPlayer

inline val mc: Minecraft
    get() = Minecraft.getInstance()

val player: LocalPlayer?
    get() = mc.player

inline val self: User
    get() = mc.user