package com.busted_moments.core.http.api.player;

import com.busted_moments.core.http.api.Printable;

public enum Dungeon implements Printable {
   //Legacy Dungeons
   AIR("Air"),
   ANIMAL("Animal"), // Lost Sanctuary
   EARTH("Earth"),
   FIRE("Fire"),
   ICE("Ice"), // Ice Barrows
   JUNGLE("Jungle"), // Undergrowth Ruins
   OCEAN("Ocean"), // Galleon's Graveyard
   SILVERFISH("Silverfish"), // Sand-Swept Tomb
   SKELETON("Skeleton"), // Decrepit Sewers
   SPIDER("Spider"), // Infested Pit
   WATER("Water"),
   ZOMBIE("Zombie"), // Underworld Crypt

   DECREPIT_SEWERS("Decrepit Sewers"),
   INFESTED_PIT("Infested Pit"),
   UNDERWORLD_CRYPT("Underworld Crypt"),
   TIMELOST_SANCTUM("Timelost Sanctum"),
   SAND_SWEPT_TOMB("Sand-Swept Tomb"),
   ICE_BARROWS("Ice Barrows"),
   UNDERGROWTH_RUINS("Undergrowth Ruins"),
   GALLEONS_GRAVEYARD("Galleon's Graveyard"),
   CORRUPTED_DECREPIT_SEWERS("Corrupted Decrepit Sewers"),
   CORRUPTED_INFESTED_PIT("Corrupted Infested Pit"),
   CORRUPTED_LOST_SANCTUARY("Corrupted Lost Sanctuary"),
   CORRUPTED_UNDERWORLD_CRYPT("Corrupted Underworld Crypt"),
   CORRUPTED_SAND_SWEPT_TOMB("Corrupted Sand-Swept Tomb"),
   FALLEN_FACTORY("Fallen Factory"),
   CORRUPTED_ICE_BARROWS("Corrupted Ice Barrows"),
   CORRUPTED_UNDERGROWTH_RUINS("Corrupted Undergrowth Ruins"),
   CORRUPTED_GALLEONS_GRAVEYARD("Corrupted Galleon's Graveyard"),
   ELDRITCH_OUTLOOK("Eldritch Outlook");

   private final String prettyPrint;

   Dungeon(String prettyPrint) {
      this.prettyPrint = prettyPrint;
   }

   public String prettyPrint() {
      return prettyPrint;
   }
}
