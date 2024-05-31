package com.busted_moments.client.features.commands;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;

import java.util.HashMap;

@Default(State.ENABLED)
@Config.Category("Commands")
@Feature.Definition(name = "Extended Online Info", description = "Extends the amount of information provided by /fuy om.")
public class ExtendedGuildOnlineMembersFeature extends Feature {
   @Instance
   private static ExtendedGuildOnlineMembersFeature THIS;

   public static ExtendedGuildOnlineMembersFeature get() {
      return THIS;
   }
   
   private HashMap<String, String> lastKnownWorld = new HashMap<String, String>();
   
   public String getLastKnownServer(String name) {
	   return lastKnownWorld.get(name);
   }
   
   public String setLastKnownServer(String name, String world) {
	   lastKnownWorld.put(name,world);
	   return world;
   }
   
}
