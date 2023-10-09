package com.busted_moments.client.models.raids.events;

import com.busted_moments.client.models.raids.Raid;
import com.busted_moments.client.models.raids.rooms.Room;

public abstract class RoomEvent extends RaidEvent {
   private final Room room;

   public RoomEvent(Raid raid, Room room) {
      super(raid);

      this.room = room;
   }

   public Room getRoom() {
      return room;
   }

   public RoomEvent() {
      this.room = null;
   }

   public static class Start extends RoomEvent {
      public Start(Raid raid, Room room) {
         super(raid, room);
      }

      @Deprecated
      public Start() {
         this(null, null);
      }
   }

   public static class Complete extends RoomEvent {
      public Complete(Raid raid, Room room) {
         super(raid, room);
      }

      @Deprecated
      public Complete() {
         this(null, null);
      }
   }
}
