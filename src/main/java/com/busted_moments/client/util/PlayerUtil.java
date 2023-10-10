package com.busted_moments.client.util;

import net.minecraft.core.Position;
import net.minecraft.world.effect.MobEffect;

import static com.wynntils.utils.mc.McUtils.player;

public class PlayerUtil {
   public static boolean isPlayer(String username) {
      return username.matches("[a-zA-Z0-9_]+");
   }

   public static boolean isNear(Position position, int radius) {
      return EntityUtil.isNear(player(), position, radius);
   }

   public static boolean isInside(Position pos1, Position pos2) {
      return EntityUtil.isInside(player(), pos1, pos2);
   }

   public static boolean hasEffect(MobEffect effect) {
      var player = player();

      return player != null && player.getActiveEffectsMap().containsKey(effect);
   }
}
