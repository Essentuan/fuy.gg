package com.busted_moments.mixin;

import com.busted_moments.client.events.chat.LogChatMessageEvent;
import com.busted_moments.client.framework.events.EventsKt;
import com.wynntils.core.WynntilsMod;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.ChatHandler;
import com.wynntils.handlers.chat.type.MessageType;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ChatHandler.class, remap = false)
public abstract class ChatHandlerMixin {
   @Redirect(method = "postChatLine", at = @At(value = "INVOKE", target = "Lcom/wynntils/core/WynntilsMod;info(Ljava/lang/String;)V"))
   private void info(String message) {}

   @Inject(
           method = "postChatLine",
           at = @At(
                   value = "INVOKE",
                   target = "Lcom/wynntils/core/WynntilsMod;info(Ljava/lang/String;)V", shift = At.Shift.BEFORE)
   )
   private void postChatLine(StyledText styledText, MessageType messageType, CallbackInfoReturnable<StyledText> cir) {
      if (!EventsKt.post(
              new LogChatMessageEvent(
                      -1,
                      styledText.getComponent(),
                      styledText,
                      LogChatMessageEvent.Source.WYNNTILS
              )
      ))
         WynntilsMod.info("[CHAT] " + styledText.getString().replace("§", "&"));
   }
}
