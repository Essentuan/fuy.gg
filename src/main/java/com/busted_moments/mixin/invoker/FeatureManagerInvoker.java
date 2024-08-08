package com.busted_moments.mixin.invoker;

import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.FeatureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = FeatureManager.class, remap = false)
public interface FeatureManagerInvoker {
   @Invoker()
   void invokeRegisterFeature(Feature feature);
}
