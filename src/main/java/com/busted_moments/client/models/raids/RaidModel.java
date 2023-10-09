package com.busted_moments.client.models.raids;

import com.busted_moments.client.models.raids.events.RaidEvent;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Model;
import com.wynntils.mc.event.TitleSetTextEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RaidModel extends Model {
   private static Raid current = null;

   @Hidden("Personal Bests")
   private static Map<RaidType, Raid> PBs = new HashMap<>();

   public static Optional<Raid> current() {
      return Optional.ofNullable(current);
   }

   public static Optional<Raid> getPB(RaidType type) {
      return Optional.ofNullable(PBs.get(type));
   }

   public static void stop() {
      if (current != null) {
         current.close();
         current = null;
      }
   }

   @SubscribeEvent
   private static void onTitle(TitleSetTextEvent event) {
      RaidType.from(event.getComponent()).ifPresent(type -> {
         if (current == null) {
            current = new Raid(type);
            current.start();
         }
      });
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   private static void onRaidComplete(RaidEvent.Complete event) {
      current = null;

      var raid = event.getRaid();
      if (event.isPB()) PBs.put(raid.type(), raid);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   private static void onRaidFail(RaidEvent.Fail event) {
      current = null;
   }

   @SubscribeEvent
   private static void onWorldState(WorldStateEvent event) {
      if (current != null) {
         current.close();

         ChatUtil.message("Tracking for %s has failed!", current.type(), ChatFormatting.RED);

         current = null;
      }
   }
}
