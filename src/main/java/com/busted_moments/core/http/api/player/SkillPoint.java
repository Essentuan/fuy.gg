package com.busted_moments.core.http.api.player;

import com.busted_moments.core.http.api.Printable;

public enum SkillPoint implements Printable {
   STRENGTH("Strength"),
   DEXTERITY("Dexterity"),
   INTELLIGENCE("Intelligence"),
   DEFENSE("Defense"),
   AGILITY("Agility");

   private final String prettyPrint;

   SkillPoint(String prettyPrint) {
      this.prettyPrint = prettyPrint;
   }

   @Override
   public String prettyPrint() {
      return prettyPrint;
   }
}
