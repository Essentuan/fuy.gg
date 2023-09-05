package com.busted_moments.client.models.war.timer.events;

import com.busted_moments.client.models.war.timer.Timer;
import com.busted_moments.core.events.BaseEvent;
import net.minecraftforge.eventbus.api.Cancelable;

@Cancelable
public class TimerStartEvent extends BaseEvent {
   private final Timer timer;

   public TimerStartEvent(Timer timer) {
      this.timer = timer;
   }

   public Timer getTimer() {
      return timer;
   }

   public TimerStartEvent() {
      this(null);
   }
}
