package com.busted_moments.client.models.raids.rooms.types;

import com.busted_moments.client.models.raids.rooms.BaseBuilder;
import com.busted_moments.client.models.raids.rooms.Room;
import com.busted_moments.client.util.PlayerUtil;
import com.wynntils.mc.event.TickEvent;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class InsideType extends Room {
   private final Position pos1;
   private final Position pos2;

   protected InsideType(String title, Position pos1, Position pos2) {
      super(title);

      this.pos1 = pos1;
      this.pos2 = pos2;
   }

   private boolean isInside = false;

   @SubscribeEvent
   public void onTick(TickEvent event) {
      boolean inside = PlayerUtil.isInside(pos1, pos2);

      if (isInside && !inside)
         complete();
      else isInside = inside;
   }

   public static class Builder extends BaseBuilder {
      private Position pos1;
      private Position pos2;

      public Builder(String title) {
         super(title);
      }

      public Builder box(double x1, double y1, double z1, double x2, double y2, double z2) {
         return box(new Vec3(x1, y1, z1), new Vec3(x2, y2, z2));
      }

      public Builder box(Position pos1, Position pos2) {
         this.pos1 = pos1;
         this.pos2 = pos2;

         return this;
      }

      public Builder rectangle(double x1, double z1, double x2, double z2) {
         return box(
                 x1,
                 1000,
                 z1,
                 x2,
                 -1000,
                 z2
         );
      }

      public Builder rectangle(Vec2 pos1, Vec2 pos2) {
         return rectangle(
                 pos1.x,
                 pos1.y,
                 pos2.x,
                 pos2.y
         );
      }

      @Override
      public Room build() {
         return new InsideType(title, pos1, pos2);
      }
   }

   public InsideType() {
      this(null, null, null);
   }
}
