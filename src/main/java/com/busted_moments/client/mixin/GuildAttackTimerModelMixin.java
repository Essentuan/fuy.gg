package com.busted_moments.client.mixin;

import com.busted_moments.client.models.war.timer.events.ScoreboardTimerAddEvent;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.scoreboard.ScoreboardSegment;
import com.wynntils.models.territories.GuildAttackTimerModel;
import com.wynntils.models.territories.TerritoryAttackTimer;
import com.wynntils.models.territories.profile.TerritoryProfile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

@Mixin(GuildAttackTimerModel.class)
public abstract class GuildAttackTimerModelMixin {
   @Inject(
           method = "processScoreboardChanges",
           at = @At(
                   value = "INVOKE",
                   target = "Lcom/wynntils/core/WynntilsMod;postEvent(Lnet/minecraftforge/eventbus/api/Event;)Z"
           ),
           locals = LocalCapture.CAPTURE_FAILHARD,
           remap = false
   )
   private void onScoreboardTimerAdd(
           ScoreboardSegment segment,
           CallbackInfo ci,
           Set<String> usedTerritories,
           Iterator<StyledText> var3,
           StyledText line,
           Matcher matcher,
           String shortTerritoryName,
           Optional<TerritoryAttackTimer> chatTimerOpt,
           TerritoryProfile territoryProfile,
           String fullTerritoryName,
           int minutes,
           int seconds,
           long timerEnd,
           TerritoryAttackTimer timer,
           TerritoryAttackTimer oldTimer
   ) {
      new ScoreboardTimerAddEvent(timer).post();
   }
}
