package com.busted_moments.mixin;

import com.wynntils.core.events.EventBusWrapper;
import net.neoforged.bus.BusBuilderImpl;
import net.neoforged.bus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EventBusWrapper.class, remap = false)
public abstract class EventBusWrapperMixin extends EventBus {
   public EventBusWrapperMixin(BusBuilderImpl busBuilder) {
      super(busBuilder);
   }

   @Inject(
           method = "register",
           at = @At("HEAD"),
           cancellable = true
   )
   private void register(Object target, CallbackInfo ci) {
      super.register(target);
      ci.cancel();
   }
}
