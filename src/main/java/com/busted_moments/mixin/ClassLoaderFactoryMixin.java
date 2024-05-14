package com.busted_moments.mixin;

import net.minecraftforge.eventbus.ClassLoaderFactory;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Mixin(value = ClassLoaderFactory.class, remap = false)
public abstract class ClassLoaderFactoryMixin {
   @Inject(
           method = "create",
           at = @At("HEAD"),
           cancellable = true
   )
   public void create(Method method, Object target, CallbackInfoReturnable<IEventListener> cir) {
      int modifiers = method.getModifiers();

      if (Modifier.isPublic(modifiers))
         return;

      String uniqueName = ((ClassLoaderFactory) (Object) this).getUniqueName(method);

      if (Modifier.isStatic(modifiers))
         cir.setReturnValue(
                 new IEventListener() {
                    @Override
                    public void invoke(Event event) {
                       try {
                          method.invoke(null, event);
                       } catch (IllegalAccessException | InvocationTargetException e) {
                          throw new RuntimeException(e);
                       }
                    }

                    @Override
                    public String listenerName() {
                       return uniqueName;
                    }
                 }
         );
      else
         cir.setReturnValue(
                 new IEventListener() {
                    @Override
                    public void invoke(Event event) {
                       try {
                          method.invoke(target, method);
                       } catch (IllegalAccessException | InvocationTargetException e) {
                          throw new RuntimeException(e);
                       }
                    }

                    @Override
                    public String listenerName() {
                       return uniqueName;
                    }
                 }
         );
   }
}
