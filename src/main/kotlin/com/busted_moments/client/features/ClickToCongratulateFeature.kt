package com.busted_moments.client.features

import com.busted_moments.buster.api.PlayerType
import com.busted_moments.buster.api.Profile
import com.busted_moments.client.framework.config.annotations.Tooltip
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.text.Text.component
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.matches
import com.busted_moments.client.framework.text.get
import com.busted_moments.client.framework.text.getValue
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent
import net.minecraft.network.chat.ClickEvent
import java.util.regex.Pattern

object ClickToCongratulateFeature : Feature() {
    private val REGULAR_PATTERN: Pattern =
        Pattern.compile("^\\[!] Congratulations to (?<player>.+) for reaching (?<type>.+) level (?<level>.+)!")
    private val PROF_PATTERN: Pattern =
        Pattern.compile("\\[!] Congratulations to (?<player>.+) for reaching level (?<level>.+) in (. )?(?<type>.+)!")

    @Value("Message")
    @Tooltip(["The message to send when congratulating people!"])
    private var message: String = "Congratulations!"

    @Subscribe
    private fun ChatMessageReceivedEvent.on() {
        originalStyledText.matches {
            any(PROF_PATTERN, REGULAR_PATTERN) { matcher, text ->
                val player by matcher
                if (!Profile.isValid(player ?: return))
                    return

                message = component {
                    +text
                    newLine()

                    +"Click to congratulate ".aqua.underline.onClick(ClickEvent.Action.RUN_COMMAND, "/msg $player $message")
                    +player!!
                    +"!"
                }
            }
        }
    }
}