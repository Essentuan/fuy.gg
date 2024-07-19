package com.busted_moments.mixin;

import kotlin.collections.ArraysKt;
import kotlin.sequences.SequencesKt;
import net.essentuan.esl.iteration.extensions.SequenceExtensionsKt;
import net.essentuan.esl.reflections.extensions.ClassExtensionsKt;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Mixin(value = EventBus.class, remap = false)
public abstract class EventBusMixin {
   @Shadow
   private ConcurrentHashMap<Object, List<EventListener>> listeners;

   @Shadow
   protected abstract void registerListener(Object target, Method method, Method real);

   @Shadow
   public abstract void register(Object target);

   @Inject(
           method = "checkSupertypes",
           at = @At("HEAD"),
           cancellable = true
   )
   private static void checkSuperTypes(Class<?> registeredType, Class<?> type, CallbackInfo ci) {
      ci.cancel();
   }

   @Inject(
           method = "register(Ljava/lang/Object;)V",
           at = @At("HEAD"),
           cancellable = true
   )
   private void register(Object target, CallbackInfo ci) {
      if (!listeners.containsKey(target)) {
         if (target instanceof Class<?> cls) {
            for (var method : cls.getDeclaredMethods()) {
               if (method.isAnnotationPresent(SubscribeEvent.class) && Modifier.isStatic(method.getModifiers())) {
                  registerListener(
                          target,
                          method,
                          method
                  );
               }
            }
         } else {
            Iterator<Class<?>> classes = ClassExtensionsKt.visit(target.getClass(), true).iterator();

            while (classes.hasNext()) {
               var cls = classes.next();

               for (var method : cls.getDeclaredMethods()) {
                  if (method.isAnnotationPresent(SubscribeEvent.class) && !Modifier.isStatic(method.getModifiers())) {
                     registerListener(
                             target,
                             method,
                             method
                     );
                  }
               }
            }
         }
      }

      ci.cancel();
   }
}
