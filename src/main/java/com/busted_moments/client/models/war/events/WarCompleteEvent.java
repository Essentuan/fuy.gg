package com.busted_moments.client.models.war.events;

import com.busted_moments.client.models.war.War;
import com.busted_moments.core.events.BaseEvent;

public class WarCompleteEvent extends WarEvent {
   public WarCompleteEvent(War war) {
      super(war);
   }

   public WarCompleteEvent() {
      this(null);
   }
}
