package com.busted_moments.client.models.war.events;

import com.busted_moments.client.models.war.War;
import com.busted_moments.core.events.BaseEvent;

public class WarStartEvent extends WarEvent{
   public WarStartEvent(War war) {
      super(war);
   }

   public WarStartEvent() {
      this(null);
   }
}