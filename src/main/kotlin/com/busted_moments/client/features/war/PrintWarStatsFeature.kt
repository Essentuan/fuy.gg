package com.busted_moments.client.features.war

import com.busted_moments.client.features.war.WarCommon.toWarString
import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.features.Replaces
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.models.territories.war.events.WarEvent
import com.wynntils.features.overlays.TowerStatsFeature
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.extensions.timeSince
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent

@Category("War")
@Replaces(TowerStatsFeature::class)
object PrintWarStatsFeature : Feature() {
    @Subscribe
    private fun WarEvent.End.on() {
        Text {
            val timeInWar = war.startedAt.timeSince().toWarString()
            val dps = war.dps(Duration.FOREVER).toCommaString().escapeCommas()

            val didDamage = war.tower.initial != war.tower.stats

            +"Time in War: ".lightPurple
                .onClick(ClickEvent.Action.COPY_TO_CLIPBOARD, timeInWar)
                .onHover(HoverEvent.Action.SHOW_TEXT, Text.component("Click to copy war duration"))
            +timeInWar.aqua

            newLine()

            +"Average DPS: ".reset.lightPurple
                .onClick(ClickEvent.Action.COPY_TO_CLIPBOARD, dps)
                .onHover(HoverEvent.Action.SHOW_TEXT, Text.component("Click to copy DPS"))
            +dps.aqua

            newLine()

            +"${if (didDamage) "Initial " else ""}Tower Stats: ".reset.lightPurple
                .onClick(
                    ClickEvent.Action.COPY_TO_CLIPBOARD,
                    Text { war.tower.initial.appendTo(this) }.stringWithoutFormatting
                )
                .onHover(
                    HoverEvent.Action.SHOW_TEXT,
                    Text.component("Click to copy ${if (didDamage) "initial " else ""}tower stats")
                )

            war.tower.initial.appendTo(this)

            if (didDamage) {
                newLine()

                val final = war.tower.stats

                +"Final Tower Stats: ".reset.lightPurple
                    .onClick(
                        ClickEvent.Action.COPY_TO_CLIPBOARD,
                        Text { final.appendTo(this) }.stringWithoutFormatting
                    )
                    .onHover(
                        HoverEvent.Action.SHOW_TEXT,
                        Text.component("Click to copy final tower stats")
                    )

                final.appendTo(this)
            }
        }.send()
    }
}