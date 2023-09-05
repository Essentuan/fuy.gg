package com.busted_moments.client.features.war;

import com.busted_moments.client.config.providers.sound.SoundProvider;
import com.busted_moments.client.models.war.timer.events.TimerStartEvent;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.client.util.Sounds;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Default(State.ENABLED)
@Config.Category("War")
@Feature.Definition(name = "War Horn", description = "Plays a sound when a war starts")
public class WarHornFeature extends Feature {
   @Dropdown(title = "Selected Sound", options = SoundProvider.class)
   private SoundEvent selected = Sounds.WAR_HORN;

   @Value("Sound volume")
   @Tooltip("The volume of the sound")
   private static Float volume = 1.0F;

   @Value("Pitch")
   @Tooltip("The pitch of the sound")
   private static Float pitch = 1.0F;

   @SubscribeEvent
   public void onTimerStart(TimerStartEvent event) {
      if (event.getTimer().isConfident()) SoundUtil.playAmbient(selected, pitch, volume);
   }
}