package com.busted_moments.core;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.heartbeat.Scheduler;
import com.busted_moments.core.heartbeat.Task;
import com.wynntils.core.components.Models;
import com.wynntils.models.worlds.type.WorldState;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.busted_moments.client.FuyMain.CONFIG;

public abstract class Model extends Config implements EventListener, Scheduler {

   public Model() {
      REGISTER_TASKS();
   }

   private static boolean HAS_STARTED = false;

   @Override
   public boolean SHOULD_EXECUTE(Task task) {
      return HAS_STARTED && Models.WorldState.getCurrentState() == WorldState.WORLD;
   }

   @SubscribeEvent
   private static void onGameStart(MinecraftStartupEvent event) {
      HAS_STARTED = true;

      CONFIG.getConfigs().forEach(config -> {
         if (config instanceof Model model) {
            model.REGISTER_EVENTS();
         }
      });
   }
}
