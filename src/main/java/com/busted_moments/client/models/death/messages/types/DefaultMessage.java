package com.busted_moments.client.models.death.messages.types;

import com.busted_moments.client.models.death.messages.DeathMessage;
import com.busted_moments.client.models.death.messages.Target;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.text.StyledText;
import net.minecraft.ChatFormatting;

public record DefaultMessage(Target target, StyledText message) implements DeathMessage {
   @Override
   public StyledText build() {
      return TextBuilder.of(target.displayName()).space().append(message.getStringWithoutFormatting(), ChatFormatting.RESET, ChatFormatting.GOLD).build();
   }
}
