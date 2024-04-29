package com.busted_moments.client;

import com.busted_moments.FuyExtension;
import com.busted_moments.core.config.ModConfig;
import com.busted_moments.core.events.BaseEvent;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.wynntils.core.WynntilsMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.Version;
import net.minecraftforge.eventbus.api.IEventBus;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Field;

public class FuyMain implements ClientModInitializer, FuyExtension {
   public static Reflections CLASS_SCANNER;

   public static final Logger LOGGER = LoggerFactory.getLogger("fuy_gg");

   public static ModConfig CONFIG;

   private static Field EVENT_BUS;

   private static ModContainer CONTAINER;

   @Override
   public void onInitializeClient() {
      ConfigurationBuilder builder = new ConfigurationBuilder();

      CONTAINER = FabricLoader.getInstance().getModContainer("fuy_gg").orElseThrow(
              () -> new RuntimeException("Where is fuy.gg? :(")
      );

      FabricLoader.getInstance()
              .getEntrypointContainers("fuy_gg", FuyExtension.class)
              .forEach(container -> builder.forPackage(container.getEntrypoint().getPackage()));

      builder.addScanners(Scanners.SubTypes, Scanners.TypesAnnotated, Scanners.MethodsAnnotated);

      CLASS_SCANNER = new Reflections(builder);

      for (Field field : WynntilsMod.class.getDeclaredFields()) {
         if (field.getName().equals("eventBus")) {
            EVENT_BUS = field;

            EVENT_BUS.setAccessible(true);

            break;
         }
      }

      if (EVENT_BUS == null) {
         throw new RuntimeException("Could not find the event bus");
      }

      BaseEvent.validate();
      Heartbeat.create();

      new ModConfig();
   }

   public static Version getVersion() {
      return CONTAINER.getMetadata().getVersion();
   }

   public static File getJar() {
      return CONTAINER.getOrigin().getPaths().get(0).toFile();
   }

   public static IEventBus getEventBus() {
      try {
         return (IEventBus) EVENT_BUS.get(null);
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   @Override
   public String getPackage() {
      return "com.busted_moments";
   }

   @Override
   public String[] getSounds() {
      return new String[]{
              "fuy:war.horn",
              "fuy:cheep.cheep"
      };
   }
}
