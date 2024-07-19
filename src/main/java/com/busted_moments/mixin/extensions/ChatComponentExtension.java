package com.busted_moments.mixin.extensions;

import com.busted_moments.client.events.chat.LogChatMessageEvent;
import com.busted_moments.client.features.chat.StackDuplicateMessagesFeature;
import com.busted_moments.client.framework.events.EventsKt;
import com.busted_moments.client.framework.text.Text;
import com.wynntils.core.text.StyledText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Mixin(ChatComponent.class)
public abstract class ChatComponentExtension {
   @Shadow
   @Final
   private List<GuiMessage> allMessages;
   @Shadow
   @Final
   private List<GuiMessage.Line> trimmedMessages;
   @Unique
   private int count = 0;
   @Unique
   private int lines = -1;
   @Unique
   private Component previous = null;

   @Redirect(
           method = "addMessageToDisplayQueue",
           at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;")
   )
   private List<FormattedCharSequence> wrapComponents(FormattedText component, int maxWidth, Font font) {
      if (!StackDuplicateMessagesFeature.INSTANCE.getEnabled() || !Objects.equals(previous, component)) {
         count = 1;
         lines = 1;
         previous = (Component) component;

         return ComponentRenderUtils.wrapComponents(component, maxWidth, font);
      }

      allMessages.removeFirst();
      trimmedMessages.subList(0, lines).clear();

      var lines = ComponentRenderUtils.wrapComponents(
              previous.copy().append(Component.literal(" (" + ++count + ")").withStyle(ChatFormatting.GRAY)),
              maxWidth,
              font
      );
      this.lines = lines.size();

      return lines;
   }

   @Inject(method = "logChatMessage", at = @At("HEAD"), cancellable = true)
   private void logChatMessage(GuiMessage message, CallbackInfo ci) {
      if (EventsKt.post(
              new LogChatMessageEvent(
                      message.addedTime(),
                      message.content(),
                      StyledText.fromComponent(message.content()),
                      LogChatMessageEvent.Source.MINECRAFT
              )
      ))
         ci.cancel();
   }
}