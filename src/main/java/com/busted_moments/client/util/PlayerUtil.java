package com.busted_moments.client.util;

import com.wynntils.utils.mc.McUtils;
import net.minecraft.core.Position;
import net.minecraft.world.effect.MobEffect;
import org.apache.commons.lang3.Range;

import static com.wynntils.utils.mc.McUtils.player;

public class PlayerUtil {
   public static boolean isPlayer(String username) {
      return username.matches("[a-zA-Z0-9_]+");
   }

   public static boolean isNear(Position position, int radius) {
      var player = player();

      return player != null && player.position().closerThan(position, radius);
   }

   public static boolean isInside(Position pos1, Position pos2) {
      var player = player();
      if (player == null) return false;

      var loc = player.position();

      return Range.of(pos1.x(), pos2.x()).contains(loc.x) &&
              Range.of(pos1.y(), pos2.y()).contains(loc.y) &&
              Range.of(pos1.z(), pos2.z()).contains(loc.z);
   }

   public static boolean hasEffect(MobEffect effect) {
      var player = player();

      return player != null && player.getActiveEffectsMap().containsKey(effect);
   }
}
