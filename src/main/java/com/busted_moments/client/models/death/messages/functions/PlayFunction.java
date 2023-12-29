package com.busted_moments.client.models.death.messages.functions;

import com.busted_moments.client.config.providers.sound.SoundProvider;
import com.busted_moments.client.features.fun.CustomDeathMessagesFeature;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.core.artemis.functions.Arg;
import com.busted_moments.core.artemis.functions.Function;
import com.wynntils.core.consumers.functions.arguments.FunctionArguments;
import net.minecraft.sounds.SoundEvent;

@Function.With(value = "play", args = @Arg(name = "sound", cls = String.class))
public class PlayFunction extends Function<String> {
   private static final SoundProvider SOUND_PROVIDER = new SoundProvider();
   public static boolean enabled = true;

   @Override
   protected String call(FunctionArguments args) {
      if (!enabled)
         return "";

      SoundEvent sound = SOUND_PROVIDER.get(args.getArgument("sound").getStringValue());

      if (sound != null)
         SoundUtil.playAmbient(
                 sound,
                 CustomDeathMessagesFeature.pitch,
                 CustomDeathMessagesFeature.volume
         );

      return "";
   }
}
