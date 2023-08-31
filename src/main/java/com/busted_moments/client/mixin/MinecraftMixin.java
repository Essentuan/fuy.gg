package com.busted_moments.client.mixin;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.client.events.mc.MinecraftStopEvent;
import com.busted_moments.core.events.BaseEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.main.GameConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
   @Shadow
   private boolean running;

   @Inject(method = "<init>", at = @At("TAIL"))
   private void onStartup(GameConfig gameConfig, CallbackInfo ci) {
      BaseEvent.init();

      new MinecraftStartupEvent().post();
   }

   @Inject(method = "stop", at = @At("HEAD"))
   private void onStop(CallbackInfo ci) {
      if (running) {
         new MinecraftStopEvent().post();
      }
   }
}
