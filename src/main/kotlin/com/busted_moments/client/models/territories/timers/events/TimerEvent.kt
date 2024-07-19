package com.busted_moments.client.models.territories.timers.events

import com.busted_moments.buster.types.guilds.AttackTimer
import net.neoforged.bus.api.Event

abstract class TimerEvent(
    val timer: AttackTimer
) : Event() {
    class Enqueued(timer: AttackTimer, val source: Source) : TimerEvent(timer)
    class ScoreboardAdded(timer: AttackTimer) : TimerEvent(timer)

    enum class Source {
        CHAT,
        BUSTER
    }
}