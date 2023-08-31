package com.busted_moments.core.events;

import com.wynntils.core.WynntilsMod;

public interface EventListener {
   default void REGISTER_EVENTS() {
      WynntilsMod.registerEventListener(this);
   }

   default void UNREGISTER_EVENTS() {
      WynntilsMod.unregisterEventListener(this);
   }
}
