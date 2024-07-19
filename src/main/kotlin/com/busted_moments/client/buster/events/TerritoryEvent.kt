package com.busted_moments.client.buster.events

import com.busted_moments.buster.api.GuildType
import com.busted_moments.buster.api.Territory
import net.neoforged.bus.api.Event

abstract class TerritoryEvent(val territory: Territory) : Event() {
    /**
     * Used when a territory was captured in-game.
     */
    class Captured(
        val territory: String,
        /**
         * The tag of the guild that took the territory.
         */
        val guild: String
    ) : Event()

    /**
     * Used when a territory was captured on the API
     */
    class Changed(
        territory: Territory,
        val before: GuildType,
    ) : TerritoryEvent(territory)
}