package com.busted_moments.client.features;

import com.busted_moments.client.util.PlayerUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.UNDERLINE;

@Default(State.ENABLED)
@Feature.Definition(name = "Click To Congratulate", description = "Click to send a congratulations message!")
public class ClickToCongratulateFeature extends Feature {
   private static final Pattern REGULAR_PATTERN = Pattern.compile("^\\[!] Congratulations to (?<player>.+) for reaching (?<type>.+) level (?<level>.+)!");
   private static final Pattern PROF_PATTERN = Pattern.compile("\\[!] Congratulations to (?<player>.+) for reaching level (?<level>.+) in (. )?(?<type>.+)!");

   @Value("Congratulations Message")
   @Tooltip("What message to use to congratulate people")
   private static String message = "Congratulations!";

   @SubscribeEvent
   public void onChatMessageReceivedEvent(ChatMessageReceivedEvent event) {
      StyledText text = event.getOriginalStyledText();

      Matcher matcher = text.getMatcher(REGULAR_PATTERN, PartStyle.StyleType.NONE);
      if (!matcher.matches() && !(matcher = text.getMatcher(PROF_PATTERN, PartStyle.StyleType.NONE)).matches())
         return;

      String player = matcher.group("player");
      if (!PlayerUtil.isPlayer(player)) return;

      event.setMessage(TextBuilder.of(event.getStyledText())
              .line()
              .append("Click to congratulate ", AQUA, UNDERLINE)
              .append(player, AQUA, UNDERLINE).append("!", AQUA, UNDERLINE)
              .onClick(
                      ClickEvent.Action.RUN_COMMAND,
                      "/msg %s %s".formatted(player, message)
              ).toComponent());
   }
}
