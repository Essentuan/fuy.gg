package com.busted_moments.client.framework.events

import com.wynntils.core.WynntilsMod
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.ICancellableEvent
import net.neoforged.bus.api.SubscribeEvent

typealias Subscribe = SubscribeEvent
typealias Cancellable = ICancellableEvent

@JvmInline
value class Events(private val obj: Any) {
    fun register() =
        WynntilsMod.registerEventListener(obj)

    fun unregister() =
        WynntilsMod.unregisterEventListener(obj)
}

val Any.events: Events
    get() = Events(this)

fun Event.post(): Boolean =
        WynntilsMod.postEvent(this)

fun Event.schedule() =
    WynntilsMod.postEventOnMainThread(this)