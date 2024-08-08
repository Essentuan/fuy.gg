package com.busted_moments.client.models.content.triggers

import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.events
import com.busted_moments.client.models.content.Trigger
import com.wynntils.models.character.event.CharacterDeathEvent

class DeathTrigger(
    val handler: () -> Unit
) : Trigger{
    init {
        events.register()
    }
    
    @Subscribe
    private fun CharacterDeathEvent.on() =
        handler()
    
    override fun close() =
        events.unregister()
}