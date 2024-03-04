package com.busted_moments.client.models.territory.eco.types;

import com.busted_moments.client.models.territory.eco.TerritoryEco;

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

   static long getStorage(ResourceType type, TerritoryEco eco) {
      if (eco == null) return 0;
      var storage = (type == ResourceType.EMERALDS) ? getEmeraldStorage(eco.isHQ()) : getResourceStorage(eco.isHQ());

      return (long) (((eco.getUpgrade(type == ResourceType.EMERALDS ? UpgradeType.EMERALD_STORAGE : UpgradeType.RESOURCE_STORAGE).bonus() / 100) + 1) * storage);
   }
}
