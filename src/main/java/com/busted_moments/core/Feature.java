package com.busted_moments.core;

import com.busted_moments.client.Client;
import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.client.features.war.AuraNotificationFeature;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.entry.ConfigEntry;
import com.busted_moments.core.config.entry.HiddenEntry;
import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.heartbeat.Scheduler;
import com.busted_moments.core.heartbeat.Task;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.util.Reflection;
import com.wynntils.core.components.Models;
import com.wynntils.models.worlds.type.WorldState;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.slf4j.Logger;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static com.busted_moments.client.Client.CONFIG;

public abstract class Feature extends Config implements EventListener, Scheduler {
   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   public @interface Definition {
      String name();

      String[] description() default "No Description Provided";

      boolean required() default false;
      String override() default "";
   }

   private final Definition definition;

   protected static final Logger LOGGER = Client.LOGGER;

   @Value("Enabled")
   private boolean enabled;

   private boolean isEnabled;

   private final List<Hud.Element> huds = new ArrayList<>();

   @SuppressWarnings("unchecked")
   public Feature() {
      super(Annotated.Required(Definition.class), Annotated.Optional(new Default.Impl(State.DISABLED)));

      this.definition = getAnnotation(Definition.class);

      for (Field field : getClass().getDeclaredFields()) {
         if (field.isAnnotationPresent(Hud.class)) {
            field.setAccessible(true);

            try {
               Hud.Element element = CONFIG.getConfig(field.getType());

               huds.add(element);

               field.set(Reflection.isStatic(field) ? null : this, element.setFeature(this));
            } catch (IllegalAccessException e) {
               throw new RuntimeException(e);
            }
         }
      }

      this.enabled = getAnnotation(Default.class, Default::value).asBoolean();

      getEntry(Feature.class, "enabled").ifPresent(entry -> {
         entry.setDefault(enabled);

         if (getEntries().stream().filter(e -> !(e instanceof HiddenEntry)).count() == 1) {
            String name = getDefinition().override().isBlank() ? getName() : getDefinition().override();
            entry.setSection(null);
            entry.setTitle(Component.literal(name));
         }
      });

      init();

      REGISTER_TASKS();
   }

   @Listener(config = Feature.class, field = "enabled")
   private void onStateChange(boolean value) {
      if (!hasStarted) return;

      setEnabled(value);
   }

   @Override
   protected boolean shouldIgnore(ConfigEntry<?> entry) {
      return entry.getField().getName().equals("enabled") && getClass().getAnnotation(Definition.class).required();
   }

   @Override
   protected String getSection() {
      return getName();
   }

   protected Definition getDefinition() {
      return (definition == null) ? getAnnotation(Definition.class) : definition;
   }


   public String getName() {
      return getDefinition().name();
   }

   @Override
   public String getKey() {
      return getName().toLowerCase();
   }

   private boolean hasFinishedInit = false;

   public boolean hasLoaded() {
      return hasFinishedInit;
   }

   public void init() {
      if (!hasFinishedInit) {
         onInit();

         hasFinishedInit = true;
      }
   }

   public boolean isEnabled() {
      return isEnabled;
   }

   public void setEnabled(boolean enabled) {
      if (isEnabled() == enabled) return;

      if (this instanceof AuraNotificationFeature) {
         LOGGER.info(String.valueOf(this));
      }

      this.enabled = enabled;
      this.isEnabled = enabled;

      if (enabled) {
         handleEnable();
      } else {
         handleDisable();
      }
   }

   public void toggle() {
      setEnabled(!isEnabled());
   }

   public void enable() {
      setEnabled(true);
   }

   public void disable() {
      setEnabled(false);
   }

   protected void handleEnable() {
      onEnable();

      huds.forEach(Hud.Element::enable);

      REGISTER_EVENTS();
   }

   protected void handleDisable() {
      UNREGISTER_EVENTS();

      huds.forEach(Hud.Element::disable);

      onDisable();
   }

   protected boolean shouldEnable() {
      return enabled;
   }

   protected void onInit() {
   }

   protected void onEnable() {
   }

   protected void onDisable() {
   }

   @Override
   public boolean SHOULD_EXECUTE(Task task) {
      return isEnabled() && Models.WorldState.getCurrentState() == WorldState.WORLD;
   }

   private static boolean hasStarted = false;

   @SubscribeEvent(priority = EventPriority.LOW)
   private static void onMinecraftStart(MinecraftStartupEvent event) {
      hasStarted = true;

      CONFIG.getConfigs().forEach(config -> {
         if (config instanceof Feature feature && feature.shouldEnable()) {
            feature.enable();
         }
      });
   }
}
