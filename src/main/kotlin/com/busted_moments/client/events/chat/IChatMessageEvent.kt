package com.busted_moments.client.events.chat

import com.wynntils.core.text.StyledText
import com.wynntils.handlers.chat.event.ChatMessageEvent

interface IChatMessageEvent {
    val originalMessage: StyledText
}

val ChatMessageEvent.Edit.originalMessage: StyledText
    get() = (this as IChatMessageEvent).originalMessage
