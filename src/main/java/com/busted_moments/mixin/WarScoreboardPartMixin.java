package com.busted_moments.mixin;

import com.busted_moments.client.models.territories.war.WarModel;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.models.war.scoreboard.WarScoreboardPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WarScoreboardPart.class, remap = false)
public abstract class WarScoreboardPartMixin {
   @Inject(method = "onSegmentChange", at = @At("HEAD"))
   private void onSegmentChange(ScoreboardSegment newValue, CallbackInfo ci) {
      WarModel.INSTANCE.start$fuy_gg();
   }

   @Inject(method = "onSegmentRemove", at = @At("HEAD"))
   private void onSegmentRemove(ScoreboardSegment newValue, CallbackInfo ci) {
      WarModel.INSTANCE.remove$fuy_gg();
   }
}
