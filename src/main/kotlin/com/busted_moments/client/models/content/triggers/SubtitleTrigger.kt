package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.framework.text.Text
import com.wynntils.mc.event.SubtitleSetTextEvent
import net.neoforged.bus.api.EventPriority

class SubtitleTrigger(
    predicate: Any,
    style: StyleType,
    handler: () -> Unit
) : TextTrigger(predicate, style, handler) {
    init {
        events.register()
    }

    @Subscribe(priority = EventPriority.HIGHEST, receiveCanceled = true)
    private fun SubtitleSetTextEvent.on() =
        test(Text(component))
    
    override fun close() {
        events.register()
    }
}