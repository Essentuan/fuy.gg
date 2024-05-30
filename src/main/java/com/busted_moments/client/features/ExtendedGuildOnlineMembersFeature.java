package com.busted_moments.client.features;

import com.busted_moments.client.features.keybinds.Keybind;
import com.busted_moments.client.util.BossbarUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.core.components.Models;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.type.WorldState;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import com.busted_moments.core.http.api.guild.Guild;

import java.util.HashMap;
import java.util.Date;

import static com.wynntils.utils.mc.McUtils.mc;

@Default(State.DISABLED)
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
