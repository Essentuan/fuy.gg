package com.busted_moments.client.events

import net.minecraftforge.eventbus.api.Event

abstract class MinecraftEvent : Event() {
    class Stop : MinecraftEvent()
}