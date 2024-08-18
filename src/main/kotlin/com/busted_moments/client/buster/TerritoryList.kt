package com.busted_moments.client.buster

import com.busted_moments.buster.api.Territory
import com.busted_moments.buster.protocol.clientbound.ClientboundMapPacket
import com.busted_moments.buster.protocol.serverbound.ServerboundTerritoryProfileUpdatePacket
import com.busted_moments.buster.types.guilds.TerritoryProfile
import com.busted_moments.client.buster.events.TerritoryEvent
import com.busted_moments.client.features.war.wynntils.GuildMapImprovementsFeature
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.events.post
import com.busted_moments.client.framework.wynntils.allTerritoryPois
import com.busted_moments.client.framework.wynntils.territoryPoiMap
import com.busted_moments.client.framework.wynntils.territoryProfileMap
import com.busted_moments.client.framework.wynntils.toTerritoryInfo
import com.busted_moments.client.inline
import com.busted_moments.mixin.invoker.TerritoryPoiInvoker
import com.wynntils.core.components.Models
import com.wynntils.mc.event.AdvancementUpdateEvent
import com.wynntils.models.worlds.event.WorldStateEvent
import com.wynntils.models.worlds.type.WorldState
import com.wynntils.services.map.pois.TerritoryPoi
import net.essentuan.esl.iteration.Iterators
import net.essentuan.esl.scheduling.api.schedule
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.tuples.numbers.FloatPair
import net.neoforged.bus.api.Event
import net.neoforged.bus.api.EventPriority
import java.util.Date
import java.util.UUID

typealias WynntilsProfile = com.wynntils.models.territories.profile.TerritoryProfile
typealias WynntilsLocation = com.wynntils.models.territories.profile.TerritoryProfile.TerritoryLocation
typealias WynntilsGuild = com.wynntils.models.territories.profile.TerritoryProfile.GuildInfo

object TerritoryList : Territory.List {
    private var territories: Territory.List? = null

    override val guilds: Map<UUID, Collection<Territory>>
        get() = territories?.guilds ?: emptyMap()
    override val size: Int
        get() = territories?.size ?: 0
    override val timestamp: Date
        get() = territories?.timestamp ?: Date(-1)

    override fun contains(element: Territory): Boolean =
        territories?.contains(element) ?: false

    override fun containsAll(elements: Collection<Territory>): Boolean =
        territories?.containsAll(elements) ?: elements.isEmpty()

    override fun get(territory: String): Territory? =
        territories?.get(territory)

    override fun get(territory: Territory): Territory? =
        territories?.get(territory)

    override fun isEmpty(): Boolean =
        size == 0

    override fun iterator(): Iterator<Territory> =
        territories?.iterator() ?: Iterators.empty()

    private fun update() {
        if (!GuildMapImprovementsFeature.enabled)
            return

        val profiles = mutableMapOf<String, WynntilsProfile>()

        Models.Territory.territoryPoiMap.clear()

        val pois = mutableSetOf<TerritoryPoi>()

        for (it in this@TerritoryList) {
            WynntilsProfile(
                it.name,
                it.name,
                WynntilsLocation(
                    it.location.start.x,
                    it.location.start.z,
                    it.location.end.x,
                    it.location.end.z
                ),
                WynntilsGuild(
                    it.owner.name,
                    it.owner.tag
                ),
                it.acquired.toInstant()
            ).also { profile ->
                profiles[it.name] = profile

                Models.Territory.territoryPoiMap[it.name] = TerritoryPoiInvoker.create(
                    { profile },
                    it.toTerritoryInfo(),
                    false
                ).also {
                    pois += it
                }
            }
        }

        Models.Territory.allTerritoryPois = pois
        Models.Territory.territoryProfileMap = profiles
    }

    @Listener
    private fun Socket.on(packet: ClientboundMapPacket) {
        val before = territories
        territories = packet.wrap()

        update()

        if (before == null)
            return

        (before.asSequence() + territories!!.asSequence())
            .map { it.name }
            .distinct()
            .forEach {
                val previous = before[it]
                val now = territories!![it] ?: return@forEach

                if (previous?.acquired != now.acquired)
                    TerritoryEvent.Changed(
                        now,
                        previous?.owner ?: GuildList.NONE,
                    ).post()
            }
    }

    @Subscribe
    private fun ProfileUpdateEvent.on() {
        inline {
            BusterService.send(
                ServerboundTerritoryProfileUpdatePacket(
                    Models.WorldState.currentWorldName,
                    profiles
                )
            )
        }
    }

    @Subscribe(priority = EventPriority.LOWEST)
    private fun AdvancementUpdateEvent.on() {
        update()
    }

    data class ProfileUpdateEvent(
        val profiles: Map<String, TerritoryProfile>
    ) : Event() {
        companion object {
            var isReady: Boolean = false
                private set

            @Subscribe
            private fun WorldStateEvent.on() {
                when (newState) {
                    WorldState.WORLD -> schedule {
                        isReady = true
                    } after 2.seconds

                    else -> isReady = false
                }
            }
        }
    }
}

val Territory.center: FloatPair
    get() = FloatPair(
        location.start.x + (location.end.x - location.start.x)/2f,
        location.start.z + (location.end.z - location.start.z)/2f,
    )