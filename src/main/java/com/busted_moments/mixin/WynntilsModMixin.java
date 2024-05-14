package com.busted_moments.mixin;

import com.busted_moments.framework.Extension;
import com.wynntils.core.WynntilsMod;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(value = WynntilsMod.class, remap = false)
public abstract class WynntilsModMixin {
   @Inject(
           method = "init",
           at = @At(
                   value = "INVOKE",
                   target = "Lcom/wynntils/core/persisted/storage/StorageManager;initComponents()V",
                   shift = At.Shift.AFTER
           )
   )
   private static void init(
           WynntilsMod.ModLoader loader,
           String modVersion,
           boolean isDevelopmentEnvironment,
           File modFile,
           CallbackInfo ci
   ) {
      FabricLoader.getInstance().getEntrypointContainers(
              "fuy_gg",
              Extension.class
      ).forEach(it -> it.getEntrypoint().init());
   }
}
