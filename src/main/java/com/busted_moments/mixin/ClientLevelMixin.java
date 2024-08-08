package com.busted_moments.mixin;

import com.busted_moments.client.events.EntityEvent;
import com.busted_moments.client.framework.events.EventsKt;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientLevel.class)
public abstract class ClientLevelMixin {
   @Inject(method = "removeEntity", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
   private void removeEntity(int entityId, Entity.RemovalReason reason, CallbackInfo ci, Entity entity) {
      if (entity != null)
         EventsKt.post(new EntityEvent.Remove(entity, reason));
   }
}
