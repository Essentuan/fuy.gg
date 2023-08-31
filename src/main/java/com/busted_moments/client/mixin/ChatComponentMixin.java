package com.busted_moments.client.mixin;

import com.busted_moments.client.events.mc.chat.MessageAddEvent;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.wynntils.utils.mc.McUtils.mc;

@Mixin(ChatComponent.class)
public abstract class ChatComponentMixin {
   @Inject(method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/GuiMessageTag;)V", at = @At("HEAD"), cancellable = true)
   private void onAddMessage(Component chatComponent, @Nullable MessageSignature headerSignature, @Nullable GuiMessageTag tag, CallbackInfo ci) {
      MessageAddEvent event = new MessageAddEvent((ChatComponent) (Object) this, chatComponent, getAllMessages(), getTrimmedMessages());
      if (!event.post()) callAddMessage(event.getMessageAsComponent(), headerSignature, mc().gui.getGuiTicks(), tag, false);

      ci.cancel();
   }

   @Invoker
   @SuppressWarnings("SameParameterValue")
   abstract void callAddMessage(Component chatComponent, @Nullable MessageSignature headerSignature, int addedTime, @Nullable GuiMessageTag tag, boolean onlyTrim);
   
   @Accessor
   public abstract List<GuiMessage> getAllMessages();

   @Accessor
   public abstract List<GuiMessage.Line> getTrimmedMessages();
}
