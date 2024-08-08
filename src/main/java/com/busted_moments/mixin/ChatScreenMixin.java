package com.busted_moments.mixin;

import com.busted_moments.client.events.chat.PlayerCommandSentEvent;
import com.busted_moments.client.framework.events.EventsKt;
import net.minecraft.client.gui.screens.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin {
   @Inject(
           method = "handleChatInput",
           at = @At(
                   value = "INVOKE",
                   target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V",
                   shift = At.Shift.BEFORE
           ),
           cancellable = true
   )
   private void handleChatInput(String message, boolean addToRecentChat, CallbackInfo ci) {
      if (EventsKt.post(new PlayerCommandSentEvent(message.substring(1))))
         ci.cancel();
   }
}
