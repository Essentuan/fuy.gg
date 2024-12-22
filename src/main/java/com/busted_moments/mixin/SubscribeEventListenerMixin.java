package com.busted_moments.mixin;

import net.neoforged.bus.SubscribeEventListener;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SubscribeEventListener.class, remap = false)
public abstract class SubscribeEventListenerMixin {
    @Shadow @Final private SubscribeEvent subInfo;

    @Redirect(method = "invoke", at = @At(value = "INVOKE", target = "Lnet/neoforged/bus/api/ICancellableEvent;isCanceled()Z"))
    private boolean isCancelled(ICancellableEvent instance) {
        return instance.isCanceled() && !subInfo.receiveCanceled();
    }
}
