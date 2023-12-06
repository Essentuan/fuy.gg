package com.busted_moments.core.http.api.player;

import com.busted_moments.core.http.api.Printable;

public interface Profession {
   int level();
   int xpPercent();

   enum Type implements Printable {
       ALCHEMISM("Alchemism"),
       ARMOURING("Armoring"),
       COMBAT("Combat"),
       COOKING("Cooking"),
       FARMING("Farming"),
       FISHING("Fishing"),
       JEWELING("Jeweling"),
       MINING("Mining"),
       SCRIBING("Scribing"),
       TAILORING("Tailoring"),
       WEAPON_SMITHING("Weapon Smithing"),
       WOODCUTTING("Wood Cutting"),
       WOODWORKING("Wood Working");

       private final String prettyPrint;

       Type(String prettyPrint) {
           this.prettyPrint = prettyPrint;
       }

       @Override
       public String prettyPrint() {
           return prettyPrint;
       }
   }
}
