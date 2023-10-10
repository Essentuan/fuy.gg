package com.busted_moments.client.mixin;

import com.busted_moments.client.events.mc.entity.EntityEvent;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
   @Inject(method = "handleSetEntityData", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
   private void onEntitySetData(ClientboundSetEntityDataPacket packet, CallbackInfo ci, Entity entity) {
      if (entity != null) new EntityEvent.SetData(entity).post();
   }
}
