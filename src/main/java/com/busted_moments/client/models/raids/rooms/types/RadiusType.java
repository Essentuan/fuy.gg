package com.busted_moments.client.models.raids.rooms.types;

import com.busted_moments.client.models.raids.rooms.BaseBuilder;
import com.busted_moments.client.models.raids.rooms.Room;
import com.busted_moments.client.util.PlayerUtil;
import com.wynntils.mc.event.TickEvent;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RadiusType extends Room {
   private final Position position;
   private final int radius;

   public RadiusType(String title, Position pos, int radius) {
      super(title);

      this.position = pos;
      this.radius = radius;
   }

   private boolean inRadius = false;

   @SubscribeEvent
   public void onTick(TickEvent event) {
      boolean near = PlayerUtil.isNear(position, radius);

      if (inRadius && !near)
         complete();
      else inRadius = near;
   }

   public static class Builder extends BaseBuilder {
      private Position position = new Vec3(0, 0, 0);
      private int radius = 10;

      public Builder(String title) {
         super(title);
      }

      public Builder at(double x, double y, double z) {
         position = new Vec3(x, y, z);

         return this;
      }

      public Builder at(Position position) {
         this.position = position;

         return this;
      }

      public Builder radius(int radius) {
         this.radius = radius;

         return this;
      }

      @Override
      public Room build() {
         return new RadiusType(title, position, radius);
      }
   }

   public RadiusType() {
      this(null, null, 0);
   }
}
