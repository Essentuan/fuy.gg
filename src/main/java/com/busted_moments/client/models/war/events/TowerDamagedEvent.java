package com.busted_moments.client.models.war.events;

import com.busted_moments.client.models.war.Tower;
import com.busted_moments.client.models.war.War;

public class TowerDamagedEvent extends WarEvent {
   private final Tower.Update update;

   public TowerDamagedEvent(War war, Tower.Update update) {
      super(war);
      this.update = update;
   }

   @SuppressWarnings("DataFlowIssue")
   public Tower.Stats getBefore() {
      return update.before();
   }

   @SuppressWarnings("DataFlowIssue")
   public Tower.Stats getAfter() {
      return update.after();
   }

   public TowerDamagedEvent() {
      this.update = null;
   }
}
