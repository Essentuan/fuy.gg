package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.Entities.isInside
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.models.content.Trigger
import com.wynntils.mc.event.TickEvent
import com.wynntils.utils.mc.McUtils.player
import net.minecraft.core.Position

class BoxTrigger(
    val start: Position,
    val end: Position,
    val condition: Boolean,
    val handler: () -> Unit
) : Trigger {
    private var inside: Boolean = player()?.isInside(start, end) == true
    
    init {
        events.register()
    }
    
    @Subscribe
    private fun TickEvent.on() {
        val previous = inside
        inside = player().isInside(start, end)
        
        if (inside != previous && inside == condition)
            handler()
    }
    
    override fun close() {
        events.unregister()
    }
}