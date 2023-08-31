package com.busted_moments.client.models.war.events;

import com.busted_moments.client.models.war.War;
import com.busted_moments.core.events.BaseEvent;

public class WarEnterEvent extends WarEvent {
   public WarEnterEvent(War war) {
      super(war);
   }

   public WarEnterEvent() {
      this(null);
   }
}