package com.busted_moments.client.util;

import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import org.apache.commons.lang3.Range;

public class EntityUtil {
   public static boolean isNear(Entity entity, Position position, int radius) {
      return entity != null && entity.position().closerThan(position, radius);
   }

   public static boolean isInside(Entity entity, Position pos1, Position pos2) {
      if (entity == null) return false;
      var loc = entity.position();

      return Range.of(pos1.x(), pos2.x()).contains(loc.x) &&
              Range.of(pos1.y(), pos2.y()).contains(loc.y) &&
              Range.of(pos1.z(), pos2.z()).contains(loc.z);
   }
}
