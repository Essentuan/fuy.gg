package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.wynntils.core.text.type.StyleType
import com.wynntils.handlers.chat.event.ChatMessageEvent

class ChatTrigger(
    predicate: Any,
    style: StyleType,
    handler: () -> Unit
) : TextTrigger(predicate, style, handler) {
    init {
        events.register()
    }
    
    @Subscribe(receiveCanceled = true)
    private fun ChatMessageEvent.Match.on() =
        test(message)
    
    override fun close() {
        events.unregister()
    }
}
