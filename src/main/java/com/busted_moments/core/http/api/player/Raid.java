package com.busted_moments.core.http.api.player;

import com.busted_moments.core.http.api.Printable;

public enum Raid implements Printable {
   THE_CANYON_COLOSSUS("The Canyon Colossus"),
   ORPHIONS_NEXUS_OF_LIGHT("Orphion's Nexus of Light"),
   NEST_OF_THE_GROOTSLANGS("Nest of the Grootslangs"),
   THE_NAMELESS_ANOMALY("The Nameless Anomaly");

   private final String prettyPrint;

   Raid(String prettyPrint) {
      this.prettyPrint = prettyPrint;
   }

   @Override
   public String prettyPrint() {
      return prettyPrint;
   }
}
