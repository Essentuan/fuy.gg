package com.busted_moments.client.features.war

import com.busted_moments.client.features.war.WarCommon.toWarString
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
import com.busted_moments.client.models.territories.war.WarModel
import com.busted_moments.client.models.territories.war.events.WarEvent
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.features.overlays.TowerStatsFeature
import com.wynntils.overlays.TowerStatsOverlay
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.color.Color
import net.essentuan.esl.format.truncate
import net.essentuan.esl.scheduling.annotations.Every
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.seconds
import net.essentuan.esl.time.extensions.timeSince

@Category("War")
@Replaces(TowerStatsFeature::class)
@Overlays(WarInfoOverlayFeature.Overlay::class)
object WarInfoOverlayFeature : Feature() {
    @Define(
        "War Info",
        at = At(
            x = 0f,
            y = 7.644165f,
        ),
        size = Size(
            width = 205f,
            height = 22.423138f
        ),
        anchor = OverlayPosition.AnchorSection.MIDDLE_LEFT,
        align = Align(
            vert = VerticalAlignment.TOP,
            horizontal = HorizontalAlignment.LEFT
        )
    )
    @Replaces(TowerStatsOverlay::class)
    object Overlay : AbstractOverlay() {
        @Value("Text Style")
        private var style: TextShadow = TextShadow.OUTLINE

        @Value("Background Color", alpha = true)
        private var background_color: Color = CustomColor(0, 0, 0, 127).esl

        override fun render(ctx: Context): Boolean {
            textbox {
                val timeInWar: Duration
                val towerEhp: Long
                val dpsMin: Double
                val dpsMax: Double
                val dps1Second: Double
                val dps5seconds: Double
                val dpsTotal: Double
                val remaining: Duration

                if (ctx.preview) {
                    timeInWar = 224.seconds
                    towerEhp = 12523563L
                    dpsMin = 48800.0
                    dpsMax = 72000.0
                    dps1Second = 0.0
                    dps5seconds = 0.0
                    dpsTotal = 0.0
                    remaining = 104.seconds
                } else {
                    timeInWar = TIME_IN_WAR
                    towerEhp = TOWER_EHP
                    dpsMin = DPS_MIN
                    dpsMax = DPS_MAX
                    dps1Second = DPS_1_SECOND
                    dps5seconds = DPS_5_SECONDS
                    dpsTotal = DPS_TOTAL
                    remaining = TIME_REMAINING
                }

                if (timeInWar < 0.seconds)
                    return@textbox false

                frame()
                
                style = this@Overlay.style
                background = background_color

                padding.all(5f)

                text = Text {
                    +"War Info".aqua
                    +" [${timeInWar.toWarString()}]".darkAqua

                    newLine()
                    newLine()

                    +"Tower EHP: ".white
                    +towerEhp.truncate().aqua

                    newLine()

                    +"Tower DPS: ".white
                    +dpsMin.truncate().aqua
                    +"-".gray
                    +dpsMax.truncate().aqua

                    newLine()
                    newLine()

                    +"Team DPS/1s: ".white
                    +dps1Second.truncate().red

                    newLine()

                    +"Team DPS/5s: ".white
                    +dps5seconds.truncate().red

                    newLine()

                    +"Team DPS (total): ".white
                    +dpsTotal.truncate().yellow

                    newLine()
                    newLine()

                    +"Estimated Time Remaining: ".white
                    +remaining.toWarString().green
                }

                true
            }

            return true
        }

        private var TIME_IN_WAR: Duration = (-1.0).seconds
        private var TOWER_EHP: Long = 0
        private var DPS_MIN: Double = 0.0
        private var DPS_MAX: Double = 0.0
        private var DPS_1_SECOND: Double = 0.0
        private var DPS_5_SECONDS: Double = 0.0
        private var DPS_TOTAL: Double = 0.0
        private var TIME_REMAINING: Duration = Duration.FOREVER

        @Subscribe
        private fun WarEvent.TowerUpdate.on() {
            val stats = war.tower.stats

            TOWER_EHP = stats.ehp
            DPS_MIN = stats.damageMin * stats.attackSpeed * 2.0
            DPS_MAX = stats.damageMax * stats.attackSpeed * 2.0
        }

        @Every(ms = 250.0)
        private fun update() {
            val war = WarModel.current

            if (war == null || !war.active) {
                TIME_IN_WAR = (-1.0).seconds

                return
            }

            TIME_IN_WAR = war.startedAt.timeSince()

            DPS_1_SECOND = war.dps(1.seconds)
            DPS_5_SECONDS = war.dps(5.seconds)
            DPS_TOTAL = war.dps(Duration.FOREVER)

            TIME_REMAINING = if (DPS_TOTAL == 0.0)
                Duration.FOREVER
            else
                (TOWER_EHP / DPS_TOTAL).seconds
        }
    }
}