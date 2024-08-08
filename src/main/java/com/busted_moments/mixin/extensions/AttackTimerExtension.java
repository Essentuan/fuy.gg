package com.busted_moments.mixin.extensions;

import com.busted_moments.buster.types.guilds.AttackTimer;
import com.busted_moments.client.models.territories.timers.ClientAttackTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = AttackTimer.class, remap = false)
public abstract class AttackTimerExtension implements ClientAttackTimer {
   @Unique
   private boolean owned = false;

   @Override
   public boolean isOwned() {
      return owned;
   }

   @Override
   public void setOwned(boolean b) {
      owned = b;
   }
}
