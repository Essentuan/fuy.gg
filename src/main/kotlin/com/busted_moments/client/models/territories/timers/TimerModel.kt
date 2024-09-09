package com.busted_moments.client.models.territories.timers

import com.busted_moments.buster.api.Territory
import com.busted_moments.buster.protocol.clientbound.ClientboundTerritoryAttackedPacket
import com.busted_moments.buster.protocol.serverbound.ServerboundTerritoryAttackedPacket
import com.busted_moments.buster.types.guilds.AttackTimer
import com.busted_moments.client.Patterns
import com.busted_moments.client.buster.BusterService
import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.buster.events.BusterEvent
import com.busted_moments.client.buster.events.TerritoryEvent
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.post
import com.busted_moments.client.framework.text.StyleType
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.text.getValue
import com.busted_moments.client.inline
import com.busted_moments.client.models.territories.timers.events.TimerEvent
import com.google.common.collect.SetMultimap
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.mc.event.ConnectionEvent.DisconnectedEvent
import com.wynntils.mc.event.InventoryMouseClickedEvent
import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.collections.maps.expireAfter
import net.essentuan.esl.collections.multimap.Multimaps
import net.essentuan.esl.collections.multimap.hashSetValues
import net.essentuan.esl.collections.setOf
import net.essentuan.esl.encoding.builtin.enumValueOf
import net.essentuan.esl.iteration.extensions.mutable.iterate
import net.essentuan.esl.orNull
import net.essentuan.esl.other.lock
import net.essentuan.esl.scheduling.annotations.Every
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.plus
import net.essentuan.esl.unsafe
import java.util.ArrayList
import java.util.Date

object TimerModel : Set<AttackTimer> {
    private val timers: SetMultimap<String, AttackTimer> =
        Multimaps.hashKeys().hashSetValues()

    private val defenses =
        mutableMapOf<String, Territory.Rating>()
            .expireAfter(10.seconds)

    private val pending =
        mutableMapOf<String, AttackTimer>()
            .expireAfter(1.seconds)


    private val queued =
        mutableMapOf<String, Boolean>()
            .expireAfter(10.seconds)
            .setOf()

    private fun enqueue(timer: AttackTimer, owned: Boolean = false, source: TimerEvent.Source): Boolean {
        if (owned)
            timer.isOwned = true

        if (source == TimerEvent.Source.CHAT) {
            pending[timer.territory] = timer
        }

        return timers.lock {
            val previous = this[timer.territory].firstOrNull {
                it.territory == timer.territory && (it.remaining - timer.remaining).abs() < 10.seconds
            }

            if (previous == null) {
                TimerEvent.Enqueued(timer, source).post()
                remove(timer.territory, previous)
                put(timer.territory, timer)
            } else {
                remove(timer.territory, previous)
                put(timer.territory,
                    previous.copy(defense = timer.defense, trusted = timer.trusted).also {
                        it.isOwned = previous.isOwned || owned
                    }
                )
            }

            previous == null
        }
    }

    @Subscribe
    private fun ChatMessageReceivedEvent.on() {
        originalStyledText.matches {
            mutate(Text::normalized) {
                Patterns.TERRITORY_DEFENSE(StyleType.DEFAULT) { matcher, _ ->
                    val territory = matcher["territory"]!!
                    val defense = unsafe {
                        enumValueOf<Territory.Rating>(matcher["defense"]!!)
                    }.orNull() ?: return

                    defenses[territory] = defense

                    timers.lock {
                        val pending = pending[territory] ?: return
                        val timer = pending.copy(defense = defense, trusted = true).also {
                            it.isOwned = pending.isOwned
                        }

                        remove(pending.territory, pending)
                        put(
                            pending.territory,
                            timer
                        )

                        inline {
                            BusterService.send(
                                ServerboundTerritoryAttackedPacket(timer)
                            )
                        }
                    }

                    return
                }

                Patterns.TIMER_START(StyleType.DEFAULT) { matcher, _ ->
                    val territory by matcher
                    val remaining = Duration(matcher["timer"]!!) ?: return

                    if (remaining <= 30.seconds)
                        return

                    var trusted = true

                    val defense = defenses[territory!!] ?: run {
                        trusted = false

                        TerritoryList[territory!!]?.defense ?: Territory.Rating.VERY_LOW
                    }

                    val timer = AttackTimer(
                        territory!!,
                        Date() + remaining,
                        defense,
                        trusted
                    )

                    if (enqueue(timer, territory in queued, TimerEvent.Source.CHAT))
                        inline {
                            BusterService.send(
                                ServerboundTerritoryAttackedPacket(timer)
                            )
                        }

                    return
                }
            }
        }
    }

    @Subscribe
    private fun TerritoryEvent.Captured.on() {
        timers.lock { removeAll(territory) }
    }

    @Subscribe
    private fun TerritoryEvent.Changed.on() {
        timers.lock { removeAll(territory) }
    }

    @Subscribe
    private fun TimerEvent.ScoreboardAdded.on() {
        inline {
            BusterService.send(
                ServerboundTerritoryAttackedPacket(
                    timer
                )
            )
        }
    }

    @Subscribe
    private fun BusterEvent.Packet.on() {
        if (packet is ClientboundTerritoryAttackedPacket)
            enqueue(packet.timer, source = TimerEvent.Source.BUSTER)
    }

    @Subscribe
    private fun InventoryMouseClickedEvent.on() {
        val screen = mc().screen
        if (hoveredSlot == null || screen == null)
            return

        val matcher = Patterns.ATTACK_SCREEN_TITLE.matcher(
            screen.title.string
        )

        if (matcher.matches())
            queued.add(matcher["territory"]!!)
    }

    @Subscribe
    private fun DisconnectedEvent.on() {
        timers.clear()
    }

    override val size: Int
        get() = timers.size()

    override fun isEmpty(): Boolean =
        timers.isEmpty

    operator fun get(territory: String): Set<AttackTimer> =
        timers[territory]

    override fun contains(element: AttackTimer): Boolean =
        timers.containsEntry(element.territory, element)

    override fun containsAll(elements: Collection<AttackTimer>): Boolean {
        for (e in elements)
            if (e !in this)
                return false

        return true
    }

    override fun iterator(): Iterator<AttackTimer> =
        timers.lock {
            val out = ArrayList<AttackTimer>(timers.size())

            values().iterate {
                if ((it.remaining + 1.5.seconds) < 100.ms)
                    remove()
                else
                    out += it
            }

            out
        }.iterator()

    @Every(seconds = 1.0)
    private fun cleanse() {
        timers.lock {
            values().iterate {
                if ((it.remaining + 1.5.seconds) < 100.ms)
                    remove()
            }
        }
    }

    var AttackTimer.isOwned: Boolean
        get() =
            (this as ClientAttackTimer).isOwned
        private set(value) {
            (this as ClientAttackTimer).isOwned = value
        }
}

interface ClientAttackTimer {
    var isOwned: Boolean
}