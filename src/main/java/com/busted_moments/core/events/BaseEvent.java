package com.busted_moments.core.events;

import com.busted_moments.core.util.Reflection;
import com.wynntils.core.WynntilsMod;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.InvocationTargetException;

import static com.busted_moments.client.FuyMain.*;

public class BaseEvent extends Event {
   public boolean post() {
      return WynntilsMod.postEvent(this);
   }

   public void cancel() {
      setCanceled(true);
   }

   public static void validate() {
      CLASS_SCANNER.getSubTypesOf(BaseEvent.class).forEach(clazz -> {
         if (!clazz.equals(BaseEvent.class) && !Reflection.hasConstructor(clazz)) {
            throw new RuntimeException("Event %s is missing default constructor".formatted(clazz.getSimpleName()));
         }
      });
   }

   @SuppressWarnings("unchecked")
   public static void init() {
      IEventBus eventBus = getEventBus();

      CLASS_SCANNER.getMethodsAnnotatedWith(SubscribeEvent.class).forEach(method -> {
         if (!Reflection.isStatic(method) || method.getParameterTypes().length != 1) return;

         method.setAccessible(true);

         SubscribeEvent annotation = method.getAnnotation(SubscribeEvent.class);

         eventBus.addListener(annotation.priority(), annotation.receiveCanceled(), (Class<? extends Event>) method.getParameterTypes()[0], event -> {
            try {
               method.invoke(null, event);
            } catch (IllegalAccessException | InvocationTargetException e) {
               throw new RuntimeException(e);
            }
         });
      });
   }
}
