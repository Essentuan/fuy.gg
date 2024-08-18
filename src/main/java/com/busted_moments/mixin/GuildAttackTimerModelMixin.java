package com.busted_moments.mixin;

import com.busted_moments.buster.api.Territory;
import com.busted_moments.buster.types.guilds.AttackTimer;
import com.busted_moments.client.buster.TerritoryList;
import com.busted_moments.client.framework.events.EventsKt;
import com.busted_moments.client.models.territories.timers.events.TimerEvent;
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

import java.util.Date;
import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;

@Mixin(value = GuildAttackTimerModel.class, remap = false)
public abstract class GuildAttackTimerModelMixin {
   @Inject(
           method = "processScoreboardChanges",
           at = @At(
                   value = "INVOKE",
                   target = "Lcom/wynntils/core/WynntilsMod;postEvent(Lnet/neoforged/bus/api/Event;)Z"
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
      Territory.Rating defense;
      Territory territory = TerritoryList.INSTANCE.get(timer.territoryName());

      if (territory == null)
         defense = Territory.Rating.VERY_LOW;
      else
         defense = territory.getDefense();

      EventsKt.post(new TimerEvent.ScoreboardAdded(
              new AttackTimer(
                      timer.territoryName(),
                      new Date(timer.timerEnd()),
                      defense,
                      false
              )
      ));
   }
}