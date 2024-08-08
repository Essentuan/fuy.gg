package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.Entities.isNear
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.models.content.Trigger
import com.wynntils.mc.event.TickEvent
import com.wynntils.utils.mc.McUtils.player
import net.minecraft.core.Position

class SphereTrigger(
    val position: Position,
    val radius: Double,
    val condition: Boolean,
    val handler: () -> Unit
) : Trigger {
    private var inside: Boolean = player()?.isNear(position, radius) ?: false
    
    init {
        events.register()
    }
    
    @Subscribe
    private fun TickEvent.on() {
        val previous = inside
        inside = player().isNear(position, radius)
        
        if (inside != previous && inside == condition)
            handler()
    }
    
    override fun close() {
        events.unregister()
    }
}