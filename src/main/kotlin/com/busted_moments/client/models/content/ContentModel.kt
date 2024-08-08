package com.busted_moments.client.models.content

import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.text.FUY_PREFIX
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.models.content.event.ContentEvent
import com.busted_moments.client.models.content.types.DungeonType
import com.busted_moments.client.models.content.types.RaidType
import com.wynntils.models.worlds.event.WorldStateEvent
import net.neoforged.bus.api.EventPriority

object ContentModel : Storage {
    var active: ContentTimer? = null
        private set

    init {
        DungeonType
        RaidType
    }

    @Subscribe(priority = EventPriority.HIGHEST)
    private fun ContentEvent.Enter.on() {
        active?.close()
        active = timer
    }
    
    @Subscribe(priority = EventPriority.HIGHEST)
    private fun ContentEvent.Finish.on() {
        active = null
    }
    
    @Subscribe(priority = EventPriority.HIGHEST)
    private fun ContentEvent.Fail.on() {
        active = null
    }
    
    @Subscribe
    private fun WorldStateEvent.on() {
        active?.also {
            it.close()
            
            FUY_PREFIX {
                +"Content timer for ${it.type.print()} has been interuptted!".red
            }.send()
        }
        
        active = null
    }
}