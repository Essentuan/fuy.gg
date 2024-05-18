package com.busted_moments.mixin;

import com.busted_moments.client.events.MinecraftEvent;
import com.busted_moments.client.framework.events.EventsKt;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
   @Shadow private volatile boolean running;

   @Inject(method = "stop", at = @At("HEAD"))
   private void stop(CallbackInfo ci) {
      if (running)
         EventsKt.post(new MinecraftEvent.Stop());
   }
}
