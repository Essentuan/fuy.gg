package com.busted_moments.client.models.territories.war.events

import net.neoforged.bus.api.Event

abstract class WarTextEvent : Event() {
    class Appear : WarTextEvent()

    class Vanish : WarTextEvent()
}