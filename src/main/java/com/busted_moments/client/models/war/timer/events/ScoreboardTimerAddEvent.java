package com.busted_moments.client.models.war.timer.events;

import com.busted_moments.core.events.BaseEvent;
import com.wynntils.models.territories.TerritoryAttackTimer;

public class ScoreboardTimerAddEvent extends BaseEvent {
   private final TerritoryAttackTimer timer;

   public ScoreboardTimerAddEvent(TerritoryAttackTimer timer) {
      this.timer = timer;
   }

   public TerritoryAttackTimer getTimer() {
      return timer;
   }

   public ScoreboardTimerAddEvent() {
      this.timer = null;
   }
}
