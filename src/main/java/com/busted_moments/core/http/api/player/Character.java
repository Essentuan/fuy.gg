package com.busted_moments.core.http.api.player;

import com.busted_moments.core.http.api.Printable;
import com.busted_moments.core.time.Duration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface Character {
   UUID uuid();

   Type type();

   Optional<String> nickname();

   int level();

   long xp();

   int xpPercent();

   int totalLevel();

   int wars();

   Duration playtime();

   long mobsKilled();

   long chestsFound();

   int logins();

   int deaths();

   int discoveries();

   Set<Modifier> modifiers();

   Map<SkillPoint, Integer> skillPoints();

   Map<Profession.Type, Profession> professions();

   Map<Dungeon, Integer> dungeons();

   Map<Raid, Integer> raids();

   Set<String> completedQuests();

   enum Type implements Printable {
      ASSASSIN("Assassin"),
      NINJA("Ninja"),
      ARCHER("Archer"),
      HUNTER("Hunter"),
      MAGE("Mage"),
      DARK_WIZARD("Dark Wizard"),
      WARRIOR("Warrior"),
      KNIGHT("Knight"),
      SHAMAN("Shaman"),
      SKYSEER("Skyseer");

      private final String prettyPrint;

      Type(String prettyPrint) {
         this.prettyPrint = prettyPrint;
      }

      @Override
      public String prettyPrint() {
         return prettyPrint;
      }
   }

   enum Modifier implements Printable {
      HARDCORE("Hardcore"),
      ULTIMATE_IRONMAN("Ultimate Ironman"),
      IRONMAN("Ironman"),
      CRAFTSMAN("Craftsman"),
      HUNTED("Hunted");

      private final String prettyPrint;

      Modifier(String prettyPrint) {
         this.prettyPrint = prettyPrint;
      }

      @Override
      public String prettyPrint() {
         return prettyPrint;
      }
   }
}
