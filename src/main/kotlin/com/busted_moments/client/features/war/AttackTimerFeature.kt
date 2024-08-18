package com.busted_moments.client.features.war

import com.busted_moments.buster.api.Territory
import com.busted_moments.buster.types.guilds.AttackTimer
import com.busted_moments.client.framework.wynntils.defenseColor
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.config.annotations.Category
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
import com.busted_moments.client.framework.render.elements.textbox
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.models.territories.TerritoryModel
import com.busted_moments.client.models.territories.timers.TimerModel
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.features.overlays.TerritoryAttackTimerOverlayFeature
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent
import com.wynntils.mc.event.TickEvent
import com.wynntils.models.territories.GuildAttackScoreboardPart
import com.wynntils.overlays.TerritoryAttackTimerOverlay
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.time.TimeUnit
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.minutes
import net.essentuan.esl.time.duration.ms
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.plus
import java.util.Date

@Category("War")
@Overlays(AttackTimerOverlayFeature.Overlay::class)
@Replaces(TerritoryAttackTimerOverlayFeature::class)
object AttackTimerOverlayFeature : Feature() {
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
        @Value("Text Style")
        private var style = TextShadow.OUTLINE

        @Value("Background Color", alpha = true)
        private var backgroundColor = CustomColor(0, 0, 0, 127).esl

        @Value("Max Timers", intMin = 0)
        private var maxTimers = 999

        private var timers: List<AttackTimer> = emptyList()

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
                    this@Overlay.timers

                if (timers.isEmpty())
                    return@textbox false

                frame()

                background = backgroundColor
                style = this@Overlay.style

                padding.all(5f)

                var rendered: Boolean = false

                text = Text {
                    for (timer in timers)
                        if (!timer.completed)
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

                rendered
            }

            return true
        }

        @Subscribe
        private fun TickEvent.on() {
            timers = TimerModel.asSequence()
                .sortedWith(
                    compareBy<AttackTimer> {
                        it.remaining
                    }.thenBy {
                        it.territory
                    }
                )
                .take(maxTimers)
                .toList()
        }
    }

    @Subscribe
    private fun ScoreboardSegmentAdditionEvent.on() {
        if (segment.scoreboardPart is GuildAttackScoreboardPart)
            isCanceled = true
    }
}

val AttackTimer.timerString: String
    get() {
        val remaining = remaining

        val minutes = remaining.getPart(TimeUnit.MINUTES).toInt()
        val seconds = remaining.getPart(TimeUnit.SECONDS).toInt()

        return "${if (minutes < 10) "0" else ""}$minutes:${if (seconds < 10) "0" else ""}$seconds"
    }