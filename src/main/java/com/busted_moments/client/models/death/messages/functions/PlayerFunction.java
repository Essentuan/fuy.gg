package com.busted_moments.client.models.death.messages.functions;

import com.busted_moments.client.models.death.messages.DeathMessageModel;
import com.busted_moments.client.models.death.messages.Target;
import com.busted_moments.core.artemis.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import net.minecraft.ChatFormatting;

@Function.With("player")
public class PlayerFunction extends Function<String> {
   @Override
   protected String call(FunctionArguments args) {
      Target target = DeathMessageModel.target();

      if (target == null)
         return "";

      return ChatFormatting.GOLD + target.displayName().getString() + ChatFormatting.RESET + ChatFormatting.GOLD;
   }
}
