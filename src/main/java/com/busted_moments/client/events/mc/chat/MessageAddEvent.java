package com.busted_moments.client.events.mc.chat;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.events.BaseEvent;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.text.StyledText;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraftforge.eventbus.api.Cancelable;

import java.util.List;

@Cancelable
public class MessageAddEvent extends BaseEvent {
   private final ChatComponent chat;

   private StyledText message;
   private Component component;
   private final List<GuiMessage> allMessages;
   private final List<GuiMessage.Line> trimmedMessages;

   public MessageAddEvent(ChatComponent chat, Component component, List<GuiMessage> allMessages, List<GuiMessage.Line> trimmedMessages) {
      this.chat = chat;
      this.message = ChatUtil.toStyledText(component);
      this.component = component;
      this.allMessages = allMessages;
      this.trimmedMessages = trimmedMessages;
   }

   public ChatComponent getChat() {
      return chat;
   }

   public StyledText getMessage() {
      return message;
   }

   public void setMessage(StyledText text) {
      this.message = text;
      this.component = text.getComponent();
   }

   public void setMessage(TextBuilder text) {
      setMessage(text.build());
   }


   public Component getMessageAsComponent() {
      return component;
   }

   public void setComponent(Component component) {
      this.component = component;
      this.message = StyledText.fromComponent(component);
   }

   public List<GuiMessage> getAllMessages() {
      return allMessages;
   }

   public List<GuiMessage.Line> getTrimmedMessages() {
      return trimmedMessages;
   }

   public MessageAddEvent() {
      this.chat = null;
      this.message = null;
      this.component = null;
      this.allMessages = null;
      this.trimmedMessages = null;
   }
}
