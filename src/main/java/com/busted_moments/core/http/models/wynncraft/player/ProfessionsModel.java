package com.busted_moments.core.http.models.wynncraft.player;

import com.busted_moments.core.http.api.player.Profession;
import com.busted_moments.core.json.BaseModel;

public class ProfessionsModel extends BaseModel implements Profession {
   @Key int level;
   @Key int xpPercent;

   @Override
   public int level() {
      return level;
   }

   @Override
   public int xpPercent() {
      return xpPercent;
   }
}
