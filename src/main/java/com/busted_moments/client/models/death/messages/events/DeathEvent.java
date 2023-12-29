package com.busted_moments.client.models.death.messages.events;

import com.busted_moments.client.models.death.messages.DeathMessage;
import com.busted_moments.client.models.death.messages.Target;
import com.busted_moments.core.events.BaseEvent;

public class DeathEvent extends BaseEvent {
   private final Target target;
   private DeathMessage message;

   public DeathEvent(DeathMessage message) {
      this.target = message.target();
      this.message = message;
   }

   public Target target() {
      return target;
   }

   public DeathMessage message() {
      return message;
   }

   public void setMessage(DeathMessage message) {
      this.message = message;
   }

   @Deprecated
   public DeathEvent() {
      this.target = null;
      this.message = null;
   }
}
