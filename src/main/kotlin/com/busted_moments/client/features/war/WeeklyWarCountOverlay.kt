package com.busted_moments.client.features.war

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
import com.busted_moments.client.framework.features.Size
import com.busted_moments.client.framework.render.elements.textbox
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.busted_moments.client.models.territories.war.WarModel
import com.wynntils.core.consumers.overlays.OverlayPosition
import com.wynntils.mc.event.TickEvent
import com.wynntils.utils.colors.CustomColor
import com.wynntils.utils.render.type.HorizontalAlignment
import com.wynntils.utils.render.type.TextShadow
import com.wynntils.utils.render.type.VerticalAlignment
import net.essentuan.esl.Rating
import net.essentuan.esl.color.Color
import net.essentuan.esl.model.annotations.Sorted
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.days
import net.essentuan.esl.time.extensions.timeSince
import net.minecraft.ChatFormatting


@Category("War")
@Overlays(WeeklyWarCountOverlay.Overlay::class)
object WeeklyWarCountOverlay : Feature() {
    @Value("Past")
    @Sorted(Rating.LOWEST)
    private var past: Duration = 7.days

    @Define(
        name = "Weekly War Overlay",
        at = At(
            x = -250f,
            y = 0f
        ),
        size = Size(
            width = 91.5f,
            height = 14.415209f
        ),
        anchor = OverlayPosition.AnchorSection.BOTTOM_MIDDLE,
        align = Align(
            vert = VerticalAlignment.BOTTOM,
            horizontal = HorizontalAlignment.RIGHT
        )
    )
    object Overlay : AbstractOverlay() {
        @Value("Text Style")
        private var style: TextShadow = TextShadow.OUTLINE

        @Value("Text Color")
        private var textColor: Color = CustomColor.fromInt(ChatFormatting.LIGHT_PURPLE.color!!).esl

        @Value("Background Color", alpha = true)
        private var backgroundColor: Color = CustomColor(0, 0, 0, 127).esl

        private var wars: Int = 0
        
        override fun render(ctx: Context): Boolean {
            textbox {
                frame()
                
                style = this@Overlay.style
                background = backgroundColor
                padding.all(5f)
                
                text = Text {
                    +wars.toCommaString().escapeCommas().color(textColor)
                    +" War"

                    if (wars != 1)
                        +"s"
                }
            }

            return true
        }

        @Subscribe
        private fun TickEvent.on() {
            wars = WarModel.count { it.started.timeSince() < past }
        }
    }
}