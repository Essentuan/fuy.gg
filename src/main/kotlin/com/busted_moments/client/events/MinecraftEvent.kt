package com.busted_moments.client.events

import net.neoforged.bus.api.Event

abstract class MinecraftEvent : Event() {
    class Stop : MinecraftEvent()
}