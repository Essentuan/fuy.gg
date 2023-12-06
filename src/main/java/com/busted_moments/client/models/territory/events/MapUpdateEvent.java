package com.busted_moments.client.models.territory.events;

import com.busted_moments.core.http.requests.mapstate.MapState;
import com.busted_moments.core.events.BaseEvent;

public class MapUpdateEvent extends BaseEvent {
   private final MapState list;

   public MapUpdateEvent(MapState list) {
      this.list = list;
   }

   public MapState getState() {
      return list;
   }

   public MapUpdateEvent() {
      this.list = MapState.empty();
   }
}
