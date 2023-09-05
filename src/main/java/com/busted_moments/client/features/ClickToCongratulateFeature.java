package com.busted_moments.client.features;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.ChatFormatting.AQUA;

@Default(State.ENABLED)
@Feature.Definition(name = "Click To Congratulate", description = "Click to send a congratulations message!")
public class ClickToCongratulateFeature extends Feature {
   private static final Pattern CONGRATULATIONS_PATTERN = Pattern.compile("^\\[!\\] Congratulations to (?<player>.+) for reaching (?<type>.+) level (?<level>.+)!");

   @Value("Congratulations Message")
   @Tooltip("What message to use to congratulate people")
   private static String message = "Congratulations!";

   @SubscribeEvent
   public void ChatMessageReceivedEvent(ChatMessageReceivedEvent event) {
      Matcher matcher = event.getOriginalStyledText().getMatcher(CONGRATULATIONS_PATTERN, PartStyle.StyleType.NONE);
      if (!matcher.matches()) return;

      String player = matcher.group("player");

      Managers.TickScheduler.scheduleNextTick(() -> ChatUtil.message(
              TextBuilder.of("Click to congratulate ", AQUA)
                      .append(player, AQUA).append("!", AQUA)
                      .onClick(
                              ClickEvent.Action.RUN_COMMAND,
                              "/msg %s %s".formatted(player, message)
                      )
      ));
   }
}
