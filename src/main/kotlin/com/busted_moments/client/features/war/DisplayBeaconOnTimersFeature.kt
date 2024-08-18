package com.busted_moments.client.features.war

import com.busted_moments.buster.types.guilds.AttackTimer
import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.buster.center
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.marker.IconPoi
import com.busted_moments.client.framework.marker.Marker
import com.busted_moments.client.framework.render.Textures
import com.busted_moments.client.models.content.ContentModel
import com.busted_moments.client.models.territories.timers.TimerModel
import com.busted_moments.client.models.territories.timers.TimerModel.isOwned
import com.wynntils.core.components.Models
import com.wynntils.mc.event.TickEvent
import com.wynntils.models.lootrun.type.LootrunningState
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.McUtils.player
import com.wynntils.utils.mc.type.PoiLocation
import net.essentuan.esl.color.Color
import net.minecraft.world.phys.Vec3

@Category("War")
object DisplayBeaconOnTimersFeature : Feature(), Marker.Provider<IconPoi> {
    @Value("Shown Beacons")
    private var limit: Int = 999

    @Value("Hide in lootruns")
    private var hideInLootrun = true

    @Value("Hide during content")
    private var hideDuringContent = true

    @Value("Max Distance")
    private var maxDistance: Int = 1000

    private var timers: List<Marker<IconPoi>> = emptyList()

    override fun iterator(): Iterator<Marker<IconPoi>> =
        timers.iterator()

    override fun isEnabled(): Boolean =
        enabled && Models.WorldState.onWorld()

    @Subscribe
    private fun TickEvent.on() {
        if ((hideInLootrun && Models.Lootrun.state != LootrunningState.NOT_RUNNING) || (hideDuringContent && ContentModel.active != null))
            timers = emptyList()
        else {
            timers = TimerModel
                .asSequence()
                .sortedWith(
                    compareBy<AttackTimer> {
                        it.remaining
                    }.thenBy {
                        it.territory
                    }
                )
                .map {
                    it to (TerritoryList[it.territory] ?: return@map null)
                }
                .filterNotNull()
                .filter { (_, territory) ->
                    val position = player().position()

                    position.distanceTo(
                        territory.center.let {
                            Vec3(
                                it.first.toDouble(),
                                position.y,
                                it.second.toDouble()
                            )
                        }
                    ).toInt() < maxDistance
                }
                .mapIndexed { i, (it, territory) ->
                    val (x, z) = territory.center

                    val icon: Textures
                    val color: Color

                    when {
                        i == 0 -> {
                            icon = Textures.DEFEND
                            color = CommonColors.RED.esl
                        }

                        it.isOwned -> {
                            icon = Textures.DIAMOND
                            color = CustomColor(78, 245, 217).esl
                        }

                        else -> {
                            icon = Textures.SLAY
                            color = CustomColor(23, 255, 144).esl
                        }
                    }

                    Marker.icon(
                        it.territory,
                        PoiLocation(
                            x.toInt(),
                            0,
                            z.toInt()
                        ),
                        icon,
                        beaconColor = color,
                        label = it.territory,
                    )
                }
                .take(limit)
                .toList()
        }
    }
}