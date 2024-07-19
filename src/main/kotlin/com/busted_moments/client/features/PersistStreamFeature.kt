package com.busted_moments.client.features

import com.busted_moments.client.events.chat.PlayerCommandSentEvent
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.send
import com.wynntils.core.components.Models
import com.wynntils.features.chat.ChatTabsFeature
import com.wynntils.mc.event.ChatScreenSendEvent
import com.wynntils.mc.event.CommandSentEvent
import com.wynntils.models.worlds.event.WorldStateEvent
import com.wynntils.models.worlds.type.WorldState
import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.scheduling.api.schedule
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.duration.seconds

@Tooltip(["Makes /stream persist across worlds."])
object PersistStreamFeature : Feature() {
    @Persistent
    private var toggled: Boolean = false

    override fun onEnable() {
        if (toggled && !Models.WorldState.isInStream)
            mc().connection!!.sendCommand("stream")
    }

    @Subscribe
    private fun PlayerCommandSentEvent.on() {
        if (command == "stream")
            toggled = !toggled
    }

    @Subscribe
    private fun WorldStateEvent.on() {
        if (newState == WorldState.WORLD && toggled) {
            schedule {
                mc().connection!!.sendCommand("stream")
            } after 600.ms
        }
    }
}