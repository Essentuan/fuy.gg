package com.busted_moments.mixin;

import com.busted_moments.client.framework.wynntils.config.LinkedConfig;
import com.wynntils.core.persisted.PersistedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PersistedValue.class, remap = false)
public abstract class PersistedValueMixin {
   @SuppressWarnings("unchecked")
   @Inject(method = "setRaw", at = @At("HEAD"), cancellable = true)
   private void setRaw(Object value, CallbackInfo ci) {
      if (((Object) this) instanceof LinkedConfig<?>) {
         ((LinkedConfig<Object>) (Object) this).store(value);
         ci.cancel();
      }
   }
}
