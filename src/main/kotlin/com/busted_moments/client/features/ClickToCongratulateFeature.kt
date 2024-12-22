package com.busted_moments.client.features

import com.busted_moments.buster.api.Profile
import com.busted_moments.client.Patterns
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.getValue
import com.busted_moments.client.framework.wynntils.message
import com.busted_moments.client.framework.text.Text
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import net.minecraft.network.chat.ClickEvent

object ClickToCongratulateFeature : Feature() {
    @Value("Message")
    @Tooltip(["The message to send when congratulating people!"])
    private var message: String = "Congratulations!"

    @Subscribe
    private fun ChatMessageReceivedEvent.on() {
        originalStyledText.matches {
            any(
                Patterns.LEVEL_UP,
                Patterns.PROF_LEVEL_UP
            ) {
                val player by this
                if (!Profile.isValid(player ?: return))
                    return

                this@on.message = Text {
                    +it
                    newLine()

                    +"Click to congratulate ".aqua.underline.onClick(ClickEvent.Action.RUN_COMMAND, "/msg $player ${this@ClickToCongratulateFeature.message}")
                    +player!!
                    +"!"
                }
            }
        }
    }
}