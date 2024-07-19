package com.busted_moments.client.models.territories.war

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.buster.events.TerritoryEvent
import com.busted_moments.client.framework.config.Storage
import com.busted_moments.client.framework.config.account
import com.busted_moments.client.framework.config.annotations.File
import com.busted_moments.client.framework.config.annotations.Persistent
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.post
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.text.getValue
import com.busted_moments.client.models.territories.TerritoryModel
import com.busted_moments.client.models.territories.war.events.WarEvent
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import com.wynntils.mc.event.BossHealthUpdateEvent
import com.wynntils.mc.event.TickEvent
import com.wynntils.models.character.event.CharacterDeathEvent
import com.wynntils.models.worlds.event.WorldStateEvent
import com.wynntils.models.worlds.type.WorldState
import net.essentuan.esl.other.lock
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.world.BossEvent
import net.neoforged.bus.api.EventPriority
import java.util.Date
import java.util.UUID
import java.util.regex.Pattern

private val TERRITORY_CAPTURED_PATTERN: Pattern =
    Pattern.compile("^\\[WAR] \\[(?<guild>.+)] captured the territory (?<territory>.+)\\.")

private val TERRITORY_CONTROL_PATTERN: Pattern =
    Pattern.compile("^\\[INFO] \\[(?<guild>.+)] has taken control of (?<territory>.+)!")

private val CAPTURE_PATTERN =
    Pattern.compile("^\\[WAR\\] You have taken control of (?<territory>.+) from \\[(?<guild>.+)\\]!.*")

@File("{uuid}")
object WarModel : Storage, ClientboundBossEventPacket.Handler, Iterable<War.Results> {
    @Persistent
    private var wars: MutableList<War.Results> by account { mutableListOf() }

    var current: War? = null
        private set

    private lateinit var lastTerritory: Territory

    @Subscribe(priority = EventPriority.LOWEST)
    private fun WarEvent.End.on() {
        current?.let {
            it.endedAt = Date()

            if (it.hasStarted)
                synchronized(wars!!) {
                    wars!! += War.Results(
                        war.startedAt,
                        war.endedAt,
                        war.territory.name,
                        war.territory.owner.uuid,
                        it.tower.initial,
                        it.tower.stats
                    )
                }
        }
    }

    @Subscribe(receiveCanceled = true)
    private fun BossHealthUpdateEvent.on() {
        packet.dispatch(WarModel)
    }

    override fun add(
        id: UUID,
        name: Component,
        progress: Float,
        color: BossEvent.BossBarColor,
        overlay: BossEvent.BossBarOverlay,
        darkenScreen: Boolean,
        playMusic: Boolean,
        createWorldFog: Boolean
    ) {
        current?.update(Tower.Stats(Text(name)) ?: return)
    }

    override fun updateName(id: UUID, name: Component) {
        current?.update(Tower.Stats(Text(name)) ?: return)
    }

    internal fun start() {
        if (current == null)
            War(lastTerritory, Date())
                .also {
                    current = it
                    WarEvent.Enter(it).post()
                }
    }

    internal fun remove() {
        current = null
    }

    @Subscribe
    private fun ChatMessageReceivedEvent.on() {
        originalStyledText.matches {
            any(
                TERRITORY_CONTROL_PATTERN,
                TERRITORY_CAPTURED_PATTERN
            ) { matcher, _ ->
                TerritoryEvent.Captured(
                    matcher["territory"]!!,
                    matcher["guild"]!!
                ).post()

                return
            }

            CAPTURE_PATTERN { matcher, _ ->
                val territory by matcher

                if (current == null || territory != current!!.territory.name || !current!!.hasStarted || current!!.hasEnded)
                    return

                val last = current!!.tower.stats
                if (last.health != 0L)
                    current!!.update(last.copy(health = 0))

                WarEvent.End(current!!, WarEvent.End.Cause.KILLED).post()

                return
            }
        }
    }

    @Subscribe
    private fun WorldStateEvent.on() {
        if (current?.active == true && newState != WorldState.WORLD)
            WarEvent.End(current!!, WarEvent.End.Cause.HUB).post()
    }

    @Subscribe
    private fun CharacterDeathEvent.on() {
        if (current?.active == true)
            WarEvent.End(current!!, WarEvent.End.Cause.DEATH).post()
    }

    @Subscribe
    private fun TerritoryEvent.Captured.on() {
        if (current?.active == true && current!!.territory.name == territory)
            WarEvent.End(current!!, WarEvent.End.Cause.CAPTURED).post()
    }

    @Subscribe(priority = EventPriority.LOWEST)
    private fun TickEvent.on() {
        lastTerritory = TerritoryModel.inside ?: return
    }

    override fun iterator(): Iterator<War.Results> =
        wars!!.lock { toList() }.iterator()
}