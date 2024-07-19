package com.busted_moments.client.events.chat

import com.busted_moments.client.framework.events.Cancellable
import com.wynntils.core.text.StyledText
import net.minecraft.client.gui.components.ChatComponent
import net.minecraft.network.chat.Component
import net.neoforged.bus.api.Event

data class LogChatMessageEvent(
    val id: Int,
    val component: Component,
    val text: StyledText,
    val source: Source
) : Event(), Cancellable {
    enum class Source {
        WYNNTILS,
        MINECRAFT,
    }
}