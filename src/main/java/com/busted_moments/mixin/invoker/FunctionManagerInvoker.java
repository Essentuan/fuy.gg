package com.busted_moments.mixin.invoker;

import com.wynntils.core.consumers.functions.Function;
import com.wynntils.core.consumers.functions.FunctionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = FunctionManager.class, remap = false)
public interface FunctionManagerInvoker {
   @Invoker
   public void invokeRegisterFunction(Function<?> function);
}
