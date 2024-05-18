package com.busted_moments.client.framework.config

import com.terraformersmc.modmenu.api.ConfigScreenFactory
import com.terraformersmc.modmenu.api.ModMenuApi
import net.minecraft.client.gui.screens.Screen

object ConfigScreen : ModMenuApi, ConfigScreenFactory<Screen> {
    override fun getModConfigScreenFactory(): ConfigScreenFactory<*> =
        this

    override fun create(screen: Screen?): Screen =
        Config.open(screen).build()
}