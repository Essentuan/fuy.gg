package com.busted_moments.client.features.chat

import com.busted_moments.client.events.chat.LogChatMessageEvent
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.mixin.accessors.MinecraftAccessor
import com.wynntils.core.components.Models
import com.wynntils.models.worlds.type.WorldState
import net.essentuan.esl.collections.maps.expireAfter
import net.essentuan.esl.collections.setOf
import net.essentuan.esl.other.lock
import net.essentuan.esl.scheduling.annotations.Every
import net.essentuan.esl.time.duration.seconds
import net.neoforged.bus.api.EventPriority

@Category("Chat")
object CleanLogFeature : Feature() {
    private val logged: MutableSet<Int> =
        mutableMapOf<Int, Boolean>().expireAfter { 1.seconds }.setOf()

    @Subscribe
    private fun LogChatMessageEvent.on() {
        if (Models.WorldState.currentState == WorldState.NOT_CONNECTED)
            return

        if (source == LogChatMessageEvent.Source.WYNNTILS)
            MinecraftAccessor.getLogger().info("[CHAT] ${text.stringWithoutFormatting}")

        isCanceled = true
    }

//    @Subscribe(priority = EventPriority.LOWEST)
//    private fun ClientsideMessageEvent.on() {
//        if (Models.WorldState.currentState == WorldState.NOT_CONNECTED)
//            return
//
//        MinecraftAccessor.getLogger().info("[CHAT] ${styledText.stringWithoutFormatting}")
//    }

    @Every(seconds = 1.0)
    private fun cleanse() {
        logged.lock { size }
    }
}
