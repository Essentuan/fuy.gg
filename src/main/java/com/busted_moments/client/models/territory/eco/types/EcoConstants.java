package com.busted_moments.client.models.territory.eco.types;

public interface EcoConstants {
   interface Tower {
      long DAMAGE_MAX = 1500;
      long DAMAGE_MIN = 1000;
      double ATTACK_SPEED = 0.5;
      long HEALTH = 300000;
      double DEFENSE = 10;
   }

   static long getResourceStorage(boolean isHQ) {
      return isHQ ? 1500 : 300;
   }

   static long getEmeraldStorage(boolean isHQ) {
      return isHQ ? 5000 : 3000;
   }
}
