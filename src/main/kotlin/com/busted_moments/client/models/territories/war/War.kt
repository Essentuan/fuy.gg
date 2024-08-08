package com.busted_moments.client.models.territories.war

import com.busted_moments.buster.api.GuildType
import com.busted_moments.buster.api.Territory
import com.busted_moments.client.buster.GuildList
import com.busted_moments.client.framework.events.post
import com.busted_moments.client.models.territories.war.events.WarEvent
import net.essentuan.esl.json.Json
import net.essentuan.esl.model.annotations.Ignored
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.extensions.timeSince
import java.util.Date
import java.util.UUID

class War(
    val territory: Territory,
    val enteredAt: Date
) {
    lateinit var tower: Tower
        internal set
    lateinit var startedAt: Date
        internal set
    lateinit var endedAt: Date
        internal set

    val hasEnded: Boolean
        get() = ::endedAt.isInitialized

    val hasStarted: Boolean
        get() = ::startedAt.isInitialized

    val active: Boolean
        get() = hasStarted && !hasEnded

    fun update(
        stats: Tower.Stats,
        at: Date = Date()
    ) {
        if (!hasStarted) {
            startedAt = Date()

            val update = Tower.Update(
                at,
                stats,
                stats
            )

            tower = Tower(mutableListOf(update))
            WarEvent.Start(this).post()
            WarEvent.TowerUpdate(this, update).post()
        } else if (active)
            tower.update(this, stats, at)
    }

    fun dps(duration: Duration): Double = when {
        !::startedAt.isInitialized || !::tower.isInitialized || tower.size < 2 -> 0.0
        duration.isForever -> (tower.initial.ehp - tower.stats.ehp) / startedAt.timeSince().toSeconds()
        else -> {
            var first: Tower.Stats? = null
            var last: Tower.Stats? = null

            for (update in tower) {
                if (update.at.timeSince() > duration)
                    continue

                if (first == null)
                    first = update.before

                last = update.after
            }

            if (first == null || last == null)
                0.0
            else
                (first.ehp - last.ehp) / duration.toSeconds()
        }
    }

    data class Results(
        val started: Date,
        val ended: Date,
        val territory: String,
        private val uuid: UUID,
        val initial: Tower.Stats,
        val final: Tower.Stats
    ) : Json.Model {
        @Ignored
        private var _owner: GuildType? = null

        val duration: Duration
            get() = Duration(started, ended)

        val dps: Double
            get() =
                ((initial.ehp - final.ehp) / duration.toSeconds().let { if (it == 0.0) 1.0 else it })

        val owner: GuildType
            get() {
                val owner = _owner

                if (owner != null)
                    return owner
                else {
                    val guild = GuildList[uuid]

                    if (guild != null) {
                        _owner = guild

                        return guild
                    }
                }

                return object : GuildType {
                    override val name: String
                        get() = "Unknown"
                    override val tag: String
                        get() = "----"
                    override val uuid: UUID
                        get() = uuid

                }
            }
    }
}