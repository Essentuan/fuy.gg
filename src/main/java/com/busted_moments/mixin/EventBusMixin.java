package com.busted_moments.mixin;

import net.minecraftforge.eventbus.EventBus;
import org.objectweb.asm.Type;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

@Mixin(value = EventBus.class, remap = false)
public abstract class EventBusMixin {
   @Redirect(
           method = "registerObject",
           at = @At(
                   value = "INVOKE",
                   target = "Ljava/lang/Class;getMethods()[Ljava/lang/reflect/Method;"
           )
   )
   private Method[] getObjectMethods(Class<?> instance) {
      return instance.getDeclaredMethods();
   }

   @Redirect(
           method = "registerClass",
           at = @At(
                   value = "INVOKE",
                   target = "Ljava/lang/Class;getMethods()[Ljava/lang/reflect/Method;"
           )
   )
   private Method[] getClassMethods(Class<?> instance) {
      return instance.getDeclaredMethods();
   }

   @Redirect(
           method = "registerListener",
           at = @At(
                   value = "INVOKE",
                   target = "Ljava/lang/reflect/Modifier;isPublic(I)Z")
   )
   private boolean isPublic(int mod) {
      return true;
   }

   @Inject(
           method = "register(Ljava/lang/Class;Ljava/lang/Object;Ljava/lang/reflect/Method;)V",
           at = @At("HEAD")
   )
   private void register(Class<?> eventType, Object target, Method method, CallbackInfo ci) {
      if (!method.trySetAccessible()) {
         var name = target instanceof Class<?> ? ((Class<?>) target).getName() : target.getClass().getName();
         throw new IllegalArgumentException(
                 "Failed to create ASMEventHandler for private method "
                         + name
                         + "."
                         + method.getName()
                         + Type.getMethodDescriptor(method)
         );
      }
   }
}
