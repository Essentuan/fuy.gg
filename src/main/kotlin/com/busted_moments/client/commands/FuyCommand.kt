@file:Command("fuy")

package com.busted_moments.client.commands

import com.busted_moments.client.framework.artemis.Ticks
import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.screens.TestScreen
import com.essentuan.acf.core.annotations.Command
import com.essentuan.acf.core.annotations.Subcommand
import com.mojang.brigadier.context.CommandContext
import com.wynntils.utils.mc.McUtils.mc

@Subcommand("config")
private fun CommandContext<*>.config() {
    Ticks.schedule {
        mc().setScreen(Config.open(mc().screen).build())
    }
}