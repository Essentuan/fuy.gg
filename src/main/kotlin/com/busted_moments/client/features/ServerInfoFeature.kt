package com.busted_moments.client.features

import com.busted_moments.buster.api.World
import com.busted_moments.client.buster.WorldList
import com.busted_moments.client.framework.wynntils.esl
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
import com.wynntils.core.components.Models
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.features.overlays.ServerUptimeInfoOverlayFeature
import com.wynntils.mc.event.TickEvent
import com.wynntils.overlays.ServerUptimeInfoOverlay
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.time.TimeUnit
import net.essentuan.esl.time.duration.FormatFlag
import net.essentuan.esl.time.duration.minutes

@Replaces(ServerUptimeInfoOverlayFeature::class)
@Overlays(ServerInfoOverlayFeature.Overlay::class)
object ServerInfoOverlayFeature : Feature() {
    @Define(
        name = "Server Info Overlay",
        at = At(
            x = 0f,
            y = 185f
        ),
        size = Size(
            width = 385.5f,
            height = 17f
        ),
        align = Align(
            vert = VerticalAlignment.TOP,
            horizontal = HorizontalAlignment.LEFT
        ),
        anchor = OverlayPosition.AnchorSection.TOP_LEFT
    )
    @Replaces(ServerUptimeInfoOverlay::class)
    object Overlay : AbstractOverlay() {
        @Value("Text Style")
        private var style = TextShadow.OUTLINE

        @Value("Background Color", alpha = true)
        private var backgroundColor = CustomColor(0, 0, 0, 127).esl

        private var current: World? = null
        private var newest: World? = null

        override fun render(ctx: Context): Boolean {
            if (!mc().options.keyPlayerList.isDown && !ctx.preview)
                return false

            textbox {
                frame()

                background = backgroundColor
                style = this@Overlay.style

                padding.all(5f)

                text = Text {
                    line {
                        current?.also {
                            +"Your World (".white
                            +it.name.aqua
                            +"): ".white

                            val age = it.age

                            +if (age < 1.minutes)
                                age.print(FormatFlag.COMPACT, TimeUnit.SECONDS).white
                            else
                                age.print(FormatFlag.COMPACT, TimeUnit.MINUTES).white
                        }
                    }

                    newest?.also {
                        +"Newest World (".white
                        +it.name.aqua
                        +"): ".white

                        val age = it.age

                        +if (age < 1.minutes)
                            age.print(FormatFlag.COMPACT, TimeUnit.SECONDS).white
                        else
                            age.print(FormatFlag.COMPACT, TimeUnit.MINUTES).white
                    }
                }
            }

            return true
        }

        @Subscribe
        private fun onTick(event: TickEvent) {
            current = WorldList[Models.WorldState.currentWorldName]
            newest = WorldList.asSequence()
                .sortedBy { it.age }
                .firstOrNull()
        }
    }
}