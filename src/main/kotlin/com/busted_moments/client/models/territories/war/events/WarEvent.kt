package com.busted_moments.client.models.territories.war.events

import com.busted_moments.client.models.territories.war.Tower
import com.busted_moments.client.models.territories.war.War
import net.neoforged.bus.api.Event

abstract class WarEvent(val war: War) : Event() {
    class Enter(war: War) : WarEvent(war)
    class Start(war: War) : WarEvent(war)
    class TowerUpdate(war: War, val update: Tower.Update) : WarEvent(war)
    class End(war: War, val cause: Cause) : WarEvent(war) {
        enum class Cause {
            DEATH,
            HUB,
            CAPTURED,
            KILLED
        }
    }
}