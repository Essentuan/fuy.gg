package com.busted_moments.client.features.keybinds;

import com.busted_moments.core.events.EventListener;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.mojang.blaze3d.platform.InputConstants;
import com.wynntils.mc.event.TickEvent;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Date;

import static com.wynntils.utils.mc.McUtils.mc;

public abstract class Keybind extends KeyMapping implements EventListener {
   private static final Duration HELD_DURATION = Duration.of(750, TimeUnit.MILLISECONDS);

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   public @interface Definition {
      String name();
      String category() default "fuy.gg";
      int defaultKey();
      boolean evaluateOnTick() default false;
   }

   private Date lastPressed = null;
   private final boolean evaluateOnTick;

   private Keybind(String keybindName, int defaultKey, String keybindCategory, boolean evaluateOnTick) {
      super(keybindName, defaultKey, keybindCategory);

      this.evaluateOnTick = evaluateOnTick;
   }

   private Keybind(Definition definition) {
      this(
              definition.name(),
              definition.defaultKey(),
              definition.category(),
              definition.evaluateOnTick()
      );

      if (definition.defaultKey() == Integer.MAX_VALUE) {
         this.setKey(InputConstants.UNKNOWN);
      }
   }

   public Keybind(Class<? extends Keybind> keybind) {
      this(getDefinitionFromClass(keybind));
   }

   public Keybind(String keybindName, String keybindCategory, int defaultKey, boolean evaluateOnTick) {
      this(keybindName, defaultKey, keybindCategory, evaluateOnTick);
   }

   public int getClickCount() {
      return clickCount;
   }

   private Date getLastPressed() {
      return lastPressed;
   }

   public boolean evaluateOnTick() {
      return evaluateOnTick;
   }

   @Override
   public void setDown(boolean value) {
      setDown(value, false);
   }

   public void setDown(boolean value, boolean fromTick) {
      if (fromTick && !evaluateOnTick) {
         return;
      }

      if (!value && getClickCount() == 0 && !fromTick) {
         lastPressed = null;
      }

      if (isDown() != value) {
         if (value) {
            this.lastPressed = new Date();

            handleKeyDown();
         } else if (getLastPressed() != null && (getClickCount() != 0 || fromTick)) {
            handleKeyUp();
         }
      }

      this.isDown = value;
   }

   private void handleKeyDown() {
      onKeyDown();
   }

   private void handleKeyUp() {
         if (Duration.since(lastPressed).greaterThanOrEqual(HELD_DURATION)) {
            onKeyLongPress();
         } else {
            onKeyPress();
         }

         onKeyUp();
   }

   protected void onKeyDown() {}

   protected void onKeyUp() {}

   protected void onKeyLongPress() {}

   protected void onKeyPress() {}

   private boolean isPressed() {
      return !this.isUnbound() && isKeyDown(key);
   }

   @SubscribeEvent
   public void onTick(TickEvent event) {
      if (isPressed() != isDown() && evaluateOnTick) {
         setDown(isPressed(), true);
      }
   }

   public static boolean isKeyDown(int key) {
      return InputConstants.isKeyDown(mc().getWindow().getWindow(), key);
   }

   public static boolean isKeyDown(InputConstants.Key key) {
      return isKeyDown(key.getValue());
   }

   private static Definition getDefinitionFromClass(Class<? extends Keybind> keybind) {
      return keybind.getAnnotation(Definition.class);
   }
}
