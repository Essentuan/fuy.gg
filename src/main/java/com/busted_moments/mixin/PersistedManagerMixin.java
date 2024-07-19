package com.busted_moments.mixin;

import com.busted_moments.client.framework.artemis.config.LinkedConfig;
import com.wynntils.core.persisted.PersistedManager;
import com.wynntils.core.persisted.PersistedValue;
import com.wynntils.core.persisted.type.PersistedMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PersistedManager.class, remap = false)
public abstract class PersistedManagerMixin {
   @Inject(method = "getMetadata", at = @At("HEAD"), cancellable = true)
   private <T> void getMetadata(PersistedValue<T> persisted, CallbackInfoReturnable<PersistedMetadata<T>> cir) {
      if (persisted instanceof LinkedConfig<T>)
         cir.setReturnValue(((LinkedConfig<T>) persisted).getMetadata());
   }
}
