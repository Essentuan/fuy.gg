package com.busted_moments.framework.events

import com.wynntils.core.WynntilsMod
import net.minecraftforge.eventbus.api.Event

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