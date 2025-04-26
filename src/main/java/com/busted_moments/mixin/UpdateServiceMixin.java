package com.busted_moments.mixin;


import com.busted_moments.client.framework.FabricLoader;
import com.wynntils.services.athena.UpdateService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(value = UpdateService.class, remap = false)
public abstract class UpdateServiceMixin {
    @Inject(method = "tryUpdate", at = @At("HEAD"), cancellable = true)
    private void tryUpdate(CallbackInfoReturnable<CompletableFuture<UpdateService.UpdateResult>> cir) {
        try {
            if (FabricLoader.INSTANCE.isModLoaded("fuy_gg_loader"))
                cir.setReturnValue(CompletableFuture.completedFuture(UpdateService.UpdateResult.SUCCESSFUL));
        } catch (Exception e) {
            //Ignored
        }
    }
}
