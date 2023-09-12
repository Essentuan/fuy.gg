package com.busted_moments.client.models.territory.eco;

import com.busted_moments.client.models.territory.eco.types.UpgradeType;

public record Upgrade(UpgradeType type, int level) {
   public long cost() {
      return type.getLevel(level).cost();
   }

   public double bonus() {
      return type.getLevel(level).bonus();
   }
}
