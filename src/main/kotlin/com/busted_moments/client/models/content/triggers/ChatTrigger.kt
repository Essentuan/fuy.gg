package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.text.StyleType
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent

class ChatTrigger(
    predicate: Any,
    style: StyleType,
    handler: () -> Unit
) : TextTrigger(predicate, style, handler) {
    init {
        events.register()
    }
    
    @Subscribe(receiveCanceled = true)
    private fun ChatMessageReceivedEvent.on() =
        test(originalStyledText)
    
    override fun close() {
        events.unregister()
    }
}