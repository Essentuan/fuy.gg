package com.busted_moments.client.features.war

import com.busted_moments.buster.api.Territory
import com.busted_moments.buster.types.guilds.AttackTimer
import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.buster.center
import com.busted_moments.client.framework.wynntils.defenseColor
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.AbstractOverlay
import com.busted_moments.client.framework.features.Align
import com.busted_moments.client.framework.features.At
import com.busted_moments.client.framework.features.Define
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.features.Overlays
import com.busted_moments.client.framework.features.Replaces
import com.busted_moments.client.framework.features.Size
import com.busted_moments.client.framework.marker.IconPoi
import com.busted_moments.client.framework.marker.Marker
import com.busted_moments.client.framework.render.Textures
import com.busted_moments.client.framework.render.builder.RenderMode
import com.busted_moments.client.framework.render.builder.quad
import com.busted_moments.client.framework.render.builder.upload
import com.busted_moments.client.framework.render.builder.vertex
import com.busted_moments.client.framework.render.elements.textbox
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.models.GroundModel
import com.busted_moments.client.models.content.ContentModel
import com.busted_moments.client.models.territories.TerritoryModel
import com.busted_moments.client.models.territories.timers.TimerModel
import com.busted_moments.client.models.territories.timers.TimerModel.isOwned
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.wynntils.core.components.Models
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.features.overlays.TerritoryAttackTimerOverlayFeature
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent
import com.wynntils.mc.event.RenderTileLevelLastEvent
import com.wynntils.mc.event.TickEvent
import com.wynntils.models.lootrun.type.LootrunningState
import com.wynntils.models.territories.GuildAttackScoreboardPart
import com.wynntils.overlays.TerritoryAttackTimerOverlay
import com.wynntils.utils.colors.CommonColors
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.McUtils.player
import com.wynntils.utils.mc.type.PoiLocation
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.color.Color
import net.essentuan.esl.time.TimeUnit
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.minutes
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.plus
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.phys.Vec3
import java.util.Date

@Category("War")
@Overlays(AttackTimersFeature.Overlay::class)
@Replaces(TerritoryAttackTimerOverlayFeature::class)
object AttackTimersFeature : Feature(), Marker.Provider<IconPoi> {
    private val BUFFER_SOURCE = MultiBufferSource.immediate(ByteBufferBuilder(256))

    @Value("Display Beacons")
    @Tooltip(["When enabled will show a beacon for queued territories"])
    private var renderBeacons: Boolean = true

    @Value("Display Outlines")
    @Tooltip(["When enabled will outline queued territories"])
    private var renderOutlines: Boolean = true

    @Value("Max Distance")
    @Tooltip(["Sets the maximum distance timer beacons/outlines can be rendered"])
    private var maxDistance: Int = 1000

    @Value("Beacon Cap", intMin = 0)
    @Tooltip(["Sets the maximum number of beacons that can be displayed"])
    private var beaconCap = 999

    @Value("Outline Cap", intMin = 0)
    @Tooltip(["Sets the maximum number of beacons that can be displayed"])
    private var outlineCap = 3

    @Value("Overlay Cap", intMin = 0)
    private var overlayCap = 999

    private var timers = emptyList<TimerStore>()
    private var markers = emptyList<Marker<IconPoi>>()

    @Subscribe
    private fun ScoreboardSegmentAdditionEvent.on() {
        if (segment.scoreboardPart is GuildAttackScoreboardPart)
            isCanceled = true
    }

    @Subscribe
    private fun RenderTileLevelLastEvent.on() {
        upload(BUFFER_SOURCE) {
            +RenderMode.DISABLE_CULL

            pose.pushPose()
            pose.translate(-camera.position.x, -camera.position.y, -camera.position.z)

            val playerY = GroundModel.height.toFloat() + 1

            var count: Int = 0

            for ((_, territory, primary, _, distance) in timers) {
                if (distance > maxDistance)
                    continue

                if (count++ >= outlineCap)
                    break

                val start = territory.location.start
                val end = territory.location.end

                quad {
                    color = primary.with(alpha = 0.5f)

                    //North
                    vertex(start.x - 0.01, playerY + 1f, start.z - 0.01f)
                    vertex(start.x - 0.01, playerY + .3f, start.z - 0.01f)
                    vertex(end.x + 0.01, playerY + .3f, start.z - 0.01f)
                    vertex(end.x + 0.01, playerY + 1f, start.z - 0.01f)

                    //East
                    vertex(end.x + 0.01f, playerY + 1f, end.z + 0.01)
                    vertex(end.x + 0.01f, playerY + .3f, end.z + 0.01)
                    vertex(end.x + 0.01f, playerY + .3f, start.z - 0.01)
                    vertex(end.x + 0.01f, playerY + 1f, start.z - 0.01)

                    //South
                    vertex(end.x + 0.01, playerY + 1f, end.z + 0.01f)
                    vertex(end.x + 0.01, playerY + .3f, end.z + 0.01f)
                    vertex(start.x - 0.01, playerY + .3f, end.z + 0.01f)
                    vertex(start.x - 0.01, playerY + 1f, end.z + 0.01f)

                    //West
                    vertex(start.x - 0.01f, playerY + 1f, start.z - 0.01)
                    vertex(start.x - 0.01f, playerY + .3f, start.z - 0.01)
                    vertex(start.x - 0.01f, playerY + .3f, end.z + 0.01)
                    vertex(start.x - 0.01f, playerY + 1f, end.z + 0.01)
                }
            }

            +RenderMode.BLEND

            count = 0

            for ((_, territory, primary, _, distance) in timers) {
                if (distance > maxDistance)
                    continue

                if (count++ >= outlineCap)
                    break

                val start = territory.location.start
                val end = territory.location.end

                quad {
                    val from = primary.with(alpha = 0.25f)
                    val to = primary.with(alpha = 0f)

                    //North
                    vertex(start.x - 0.01, playerY + 127f, start.z - 0.01f, to)
                    vertex(start.x - 0.01, playerY + 1.3f, start.z - 0.01f, from)
                    vertex(end.x + 0.01, playerY + 1.3f, start.z - 0.01f, from)
                    vertex(end.x + 0.01, playerY + 127f, start.z - 0.01f, to)

                    //East
                    vertex(end.x + 0.01f, playerY + 127f, end.z + 0.01, to)
                    vertex(end.x + 0.01f, playerY + 1.3f, end.z + 0.01, from)
                    vertex(end.x + 0.01f, playerY + 1.3f, start.z - 0.01, from)
                    vertex(end.x + 0.01f, playerY + 127f, start.z - 0.01, to)

                    //South
                    vertex(end.x + 0.01, playerY + 127f, end.z + 0.01f, to)
                    vertex(end.x + 0.01, playerY + 1.3f, end.z + 0.01f, from)
                    vertex(start.x - 0.01, playerY + 1.3f, end.z + 0.01f, from)
                    vertex(start.x - 0.01, playerY + 127f, end.z + 0.01f, to)

                    //West
                    vertex(start.x - 0.01f, playerY + 127f, start.z - 0.01, to)
                    vertex(start.x - 0.01f, playerY + 1.3f, start.z - 0.01, from)
                    vertex(start.x - 0.01f, playerY + 1.3f, end.z + 0.01, from)
                    vertex(start.x - 0.01f, playerY + 127f, end.z + 0.01, to)
                }
            }

            pose.popPose()
        }
    }

    @Subscribe
    private fun TickEvent.on() {
        if (Models.Lootrun.state != LootrunningState.NOT_RUNNING || ContentModel.active != null)
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
                .mapIndexed { i, (it, territory) ->
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

                    val position = player().position()

                    TimerStore(
                        it,
                        territory,
                        color,
                        icon,
                        position.distanceTo(
                            territory.center.let {
                                Vec3(
                                    it.first.toDouble(),
                                    position.y,
                                    it.second.toDouble()
                                )
                            }
                        ).toInt()
                    )
                }
                .toList()

            markers = timers.asSequence()
                .filter {
                    it.distance < maxDistance
                }.map {
                    it.marker()
                }.take(beaconCap).toList()
        }
    }

    override fun isEnabled(): Boolean =
        this.enabled && renderBeacons

    override fun iterator(): Iterator<Marker<IconPoi>> =
        markers.iterator()

    @Define(
        name = "Attack Timer Overlay",
        at = At(
            x = 0f,
            y = 60f
        ),
        size = Size(
            width = 320f,
            height = 70f
        ),
        align = Align(
            vert = VerticalAlignment.TOP,
            horizontal = HorizontalAlignment.RIGHT
        ),
        anchor = OverlayPosition.AnchorSection.TOP_RIGHT
    )
    @Replaces(TerritoryAttackTimerOverlay::class)
    object Overlay : AbstractOverlay() {
        @Value("Overlay Text Style")
        private var style = TextShadow.OUTLINE

        @Value("Overlay Background Color", alpha = true)
        private var backgroundColor = CustomColor(0, 0, 0, 127).esl

        private fun example(
            territory: String,
            duration: Duration,
            defense: Territory.Rating
        ): AttackTimer = AttackTimer(
            territory,
            Date() + (duration + 500.ms),
            defense,
            true
        )

        override fun render(ctx: Context): Boolean {
            textbox {
                val timers = if (ctx.preview)
                    listOf(
                        example("Abandoned Pass", 1.minutes + 17.seconds, Territory.Rating.VERY_LOW),
                        example("Detlas Savannah Transition", 2.minutes + 47.seconds, Territory.Rating.HIGH),
                        example("Detlas", 3.minutes + 25.seconds, Territory.Rating.MEDIUM),
                        example("Almuj City", 5.minutes + 13.seconds, Territory.Rating.HIGH),
                        example("Mine Base Plains", 5.minutes + 40.seconds, Territory.Rating.VERY_HIGH),
                    )
                else
                    timers.map { it.timer }

                if (timers.isEmpty())
                    return@textbox false

                frame()

                background = backgroundColor
                style = this@Overlay.style

                padding.all(5f)

                var rendered: Boolean = false

                var count = 0

                text = Text {
                    for (timer in timers) {
                        if (timer.completed)
                            continue

                        if (count++ >= overlayCap)
                            break

                        line {
                            rendered = true

                            if (TerritoryModel.inside?.name == timer.territory)
                                +timer.territory.lightPurple.bold
                            else
                                +timer.territory.gold

                            +" (".reset.gold

                            if (timer.trusted)
                                +timer.defense.print().color(timer.defense.defenseColor)
                            else
                                +timer.defense.print().color(timer.defense.defenseColor).italicize

                            +"): ".reset.gold

                            +timer.timerString.aqua
                        }
                    }
                }

                rendered
            }

            return true
        }
    }
}

private data class TimerStore(
    val timer: AttackTimer,
    val territory: Territory,
    val color: Color,
    val icon: Textures,
    val distance: Int
) {
    fun marker(): Marker<IconPoi> {
        val center = territory.center

        return Marker.icon(
            territory.name,
            PoiLocation(
                center.first.toInt(),
                0,
                center.second.toInt()
            ),
            icon,
            beaconColor = color,
            label = territory.name,
        )
    }
}

val AttackTimer.timerString: String
    get() {
        val remaining = remaining

        val minutes = remaining.getPart(TimeUnit.MINUTES).toInt()
        val seconds = remaining.getPart(TimeUnit.SECONDS).toInt()

        return "${if (minutes < 10) "0" else ""}$minutes:${if (seconds < 10) "0" else ""}$seconds"
    }