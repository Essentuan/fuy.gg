package com.busted_moments.client.models.death.messages;

import com.busted_moments.client.models.death.messages.types.DefaultMessage;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.type.IterationDecision;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.HoverEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

import static com.busted_moments.client.features.RevealNicknamesFeature.NICK_REGEX;

public interface DeathMessage {
   Target target();

   StyledText build();

   Set<Integer> ALLOWED_COLORS = Set.of(
           //make yellow line go away
           Objects.requireNonNull(ChatFormatting.GOLD.getColor())
   );

   Set<String> DEFAULT = Set.of(
           "has died.",
           "passed away.",
           "kicked the bucket.",
           "was killed.",
           "has perished.",
           "met their demise.",
           "is six feet under.",
           "has been slain.",
           "bit the dust.",
           "met their fate."
   );

   static Optional<DeathMessage> find(StyledText text) {
      boolean[] isFirst = {true};
      boolean[] isDeathMessage = {true};
      Target[] target = {null};
      TextBuilder message = TextBuilder.empty();

      text.iterate((next, changes) -> {
         var color = next.getPartStyle().getStyle().getColor();

         if (color == null || !ALLOWED_COLORS.contains(color.getValue())) {
            isDeathMessage[0] = false;

            return IterationDecision.BREAK;
         }

         if (!isFirst[0]) {
            message.append(next);

            return IterationDecision.CONTINUE;
         }

         isFirst[0] = false;

         var hover = next.getPartStyle().getStyle().getHoverEvent();

         if (hover != null && hover.getAction() == HoverEvent.Action.SHOW_TEXT) {
            for (StyledText component : StyledText.fromComponent(hover.getValue(HoverEvent.Action.SHOW_TEXT)).split("\n")) {
               Matcher matcher = component.getMatcher(NICK_REGEX, PartStyle.StyleType.NONE);
               if (!matcher.matches())
                  continue;

               target[0] = new Target(
                       matcher.group("username"),
                       Optional.of(matcher.group("nick")),
                       StyledText.fromPart(next)
               );

               return IterationDecision.CONTINUE;
            }
         }

         String[] parts = next.getString(null, PartStyle.StyleType.NONE).split(" ");

         if (parts.length < 2) {
            isDeathMessage[0] = false;

            return IterationDecision.BREAK;
         }

         target[0] = new Target(
                 parts[0],
                 Optional.empty(),
                 StyledText.fromString(parts[0])
         );

         for (int i = 1; i < parts.length; i++) {
            message.append(parts[i]);

            if (i + 1 < parts.length)
               message.space();
         }

         return IterationDecision.CONTINUE;
      });

      if (!isDeathMessage[0] || target[0] == null)
         return Optional.empty();

      StyledText messageText = message.build().trim();

      if (!DEFAULT.contains(messageText.getString(PartStyle.StyleType.NONE)))
         return Optional.empty();

      return Optional.of(new DefaultMessage(target[0], messageText));
   }

   interface Template {
      DeathMessage format(Target target);
   }

   static DeathMessage empty(Target target) {
      return new DeathMessage() {
         @Override
         public Target target() {
            return target;
         }

         @Override
         public StyledText build() {
            return StyledText.EMPTY;
         }
      };
   }
}
