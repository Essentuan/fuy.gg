package com.busted_moments.client.models.war.events;

import com.busted_moments.client.models.war.War;
import com.busted_moments.core.events.BaseEvent;

public abstract class WarEvent extends BaseEvent {
   private final War war;

   public WarEvent(War war) {
      this.war = war;
   }

   public War getWar() {
      return war;
   }

   public WarEvent() {
      this(null);
   }
}
