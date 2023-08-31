package com.busted_moments.core.render.overlay;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.core.artemis.ArtemisFeatures;
import com.busted_moments.core.artemis.FuyFeature;
import com.busted_moments.core.render.RenderableElement;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.overlays.Overlay;
import com.wynntils.core.consumers.overlays.OverlayManager;
import com.wynntils.core.consumers.overlays.RenderState;
import com.wynntils.mc.event.RenderEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class Overlays {
   private static Method REGISTER_METHOD;

   static List<Hud.Element> overlays = new ArrayList<>();

   public static void register(Hud.Element element) {
      if (REGISTER_METHOD == null) getMethod();

      try {
         REGISTER_METHOD.invoke(
                 Managers.Overlay,
                 element.overlay,
                 FuyFeature.INSTANCE,
                 RenderEvent.ElementType.GUI,
                 RenderState.POST,
                 element.enabledByDefault
         );
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }

   private static void getMethod() {
      try {
         REGISTER_METHOD = OverlayManager.class.getDeclaredMethod("registerOverlay",
                 Overlay.class,
                 Feature.class,
                 RenderEvent.ElementType.class,
                 RenderState.class,
                 boolean.class
         );

         REGISTER_METHOD.setAccessible(true);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   private static boolean initComplete = false;

   @SubscribeEvent
   private static void onGameStart(MinecraftStartupEvent event) {
      FuyFeature.INSTANCE = new FuyFeature();

      if (initComplete) return;

      overlays.forEach(Overlays::register);

      ArtemisFeatures.register(FuyFeature.INSTANCE);

      initComplete = true;
   }

   public abstract static class Element<This extends Element<This>> implements RenderableElement<This> {
      protected float height = 0;
      protected float width = 0;

      protected float x = 0;
      protected float y = 0;

      protected float scale = 1;

      protected abstract Hud.Element getElement();

      public This setScale(float scale) {
         this.scale = scale;

         return getThis();
      }

      public float getX() {
         return x;
      }

      @Override
      public This setX(float x) {
         this.x = x;

         return getThis();
      }

      public float getY() {
         return y;
      }

      @Override
      public This setY(float y) {
         this.y = y;

         return getThis();
      }

      public float getHeight() {
         return height;
      }

      @Override
      public This setHeight(float height) {
         this.height = height;

         return getThis();
      }

      public float getWidth() {
         return width;
      }

      @Override
      public This setWidth(float width) {
         this.width = width;

         return getThis();
      }

      public This setSize(float width, float height) {
         return setWidth(width).setHeight(height);
      }

      @Override
      public float getScale() {
         return scale;
      }

      public This with(Hud.Element element) {
         return setPosition(element.getX(), element.getY())
                 .setWidth(element.getWidth())
                 .setHeight(element.getHeight());
      }

       public This build() {
         getElement().elements.add(this);

         return getThis();
      }

      public <T extends RenderableElement<?>> T then(T next) {
         build();

         return next;
      };

      public <T extends RenderableElement<?>> T then(Supplier<T> next) {
         return then(next.get());
      };
   }
}
