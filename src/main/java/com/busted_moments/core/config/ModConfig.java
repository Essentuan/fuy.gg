package com.busted_moments.core.config;

import com.busted_moments.client.events.mc.MinecraftStopEvent;
import com.busted_moments.core.Feature;
import com.busted_moments.core.Model;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.toml.Toml;
import com.busted_moments.core.util.ClassOrdering;
import com.busted_moments.core.util.Reflection;
import com.wynntils.core.components.Managers;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static com.busted_moments.client.FuyMain.CLASS_SCANNER;
import static com.busted_moments.client.FuyMain.CONFIG;
import static com.wynntils.utils.mc.McUtils.mc;

public class ModConfig implements Buildable<Screen, ConfigBuilder> {
   private final Map<Class<? extends Config>, Config> sections = new LinkedHashMap<>();
   private final File CONFIG_FILE = FabricLoader.getInstance().getConfigDir().resolve("fuy_gg.toml").toFile();

   @SuppressWarnings({"rawtypes", "unchecked"})
   public ModConfig() {
      CONFIG = this;

      CLASS_SCANNER.getSubTypesOf(Config.class).stream()
              .sorted(new ClassOrdering(
                      Comparator.comparing(Class::getSimpleName),
                      Model.class,
                      Hud.Element.class,
                      Feature.class,
                      Config.class
              )).forEach(clazz -> {
                 try {
                    if (Reflection.isAbstract(clazz)) return;

                    Constructor<Config> constructor = (Constructor<Config>) clazz.getDeclaredConstructor();
                    constructor.setAccessible(true);

                    Config config = constructor.newInstance();

                    config.getEntries().forEach(entry -> ((ConfigEntry) entry).setDefault(entry.get()));

                    sections.put(config.getClass(), config);
                 } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                          NoSuchMethodException e) {
                    throw new RuntimeException(e);
                 }
              });

      sections.forEach((clazz, config) -> config.setInstances());

      load();
   }

   @Override
   public String getKey() {
      return null;
   }

   public File getConfigFile() {
      return CONFIG_FILE;
   }

   @SuppressWarnings("unchecked")
   public <T extends Config> T getConfig(Class<?> clazz) {
      return (T) sections.get(clazz);
   }

   public Collection<Config> getConfigs() {
      return sections.values();
   }

   public void save() {
      Toml toml = Toml.empty();

      sections.values().forEach(section -> section.getEntries().forEach(entry -> entry.save(toml)));

      try {
         toml.write(CONFIG_FILE);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   public void load() {
      if (!getConfigFile().exists()) return;

      Toml config = Toml.read(getConfigFile());

      sections.values().forEach(section -> section.getEntries().forEach(entry -> entry.load(config)));
   }

   @Override
   public ConfigBuilder build(Screen screen) {
      ConfigBuilder builder = ConfigBuilder.create()
              .setTitle(Component.literal("fuy.gg | Configuration"))
              .setSavingRunnable(this::save)
              .setShouldListSmoothScroll(true)
              .setShouldTabsSmoothScroll(true)
              .setTransparentBackground(true);

      if (screen != null) builder.setParentScreen(screen);

      Map<String, ModCategory> categories = new LinkedHashMap<>();
      categories.put("General", new ModCategory("General"));

      sections.values().stream()
              .sorted(Comparator.comparing(config -> config.getClass().getSimpleName()))
              .flatMap(config -> config.getEntries().stream())
              .forEach(entry -> {
                 if (!categories.containsKey(entry.getCategory())) categories.put(entry.getCategory(), new ModCategory(entry.getCategory()));

                 categories.get(entry.getCategory()).add(entry);
              });

      categories.values().forEach(category -> category.build(builder));

      return builder;
   }

   public void open() {
      Managers.TickScheduler.scheduleNextTick(() -> mc().setScreen(CONFIG.build(mc().screen).build()));
   }

   @SubscribeEvent
   private static void onMinecraftStop(MinecraftStopEvent event) {
      CONFIG.save();
   }
}
