package com.busted_moments.client.util;

import com.busted_moments.client.util.wynntils.ChatTabUtils;
import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.components.Services;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.ClientsideMessageEvent;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Pattern;

import static com.wynntils.utils.mc.McUtils.mc;
import static net.minecraft.ChatFormatting.*;

public class ChatUtil {
   private static final Component PREFIX = component("[", DARK_GREEN)
           .append(component("f", RED))
           .append(component("u", GOLD))
           .append(component("y", YELLOW))
           .append(component(".", GREEN))
           .append(component("g", AQUA))
           .append(component("g", BLUE))
           .append(component("]", DARK_GREEN))
           .append(component(" ⋙ ", WHITE))
           .append(component("", RESET));

   public static final char COLOR_CHAR = '§';
   private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-OR]");

   public static MutableComponent create(String text, ChatFormatting... formats) {
      return create(component(text, formats));
   }

   public static void message(String message, ChatFormatting... formats) {
      message(component(message, formats));
   }

   public static void message(String message, Object... args) {
      message(component(message.formatted(args), WHITE));
   }

   public static void message(String message) {
      message(component(message, WHITE));
   }


   public static void message(TextBuilder message) {
      message(message.build());
   }


   public static void message(StyledText message) {
      if (mc().player == null) {
         return;
      }

      FontRenderer.split(message, 0).forEach(text -> mc().player.sendSystemMessage(create(text.getComponent())));
   }


   public static void message(Component message) {
      message(StyledText.fromComponent(message));
   }

   public static void send(Component component) {
      send(ChatUtil.toStyledText(component), component);
   }

   private static void send(StyledText text, Component component) {
      ChatTabUtils.getFeature().onChatReceived(new ChatMessageReceivedEvent(component, text, MessageType.BACKGROUND, RecipientType.CLIENTSIDE));
   }

   public static void send(StyledText message) {
      if (mc().player == null) {
         return;
      }

      FontRenderer.split(message, 0).forEach(text -> send(text, text.getComponent()));
   }


   public static void send(TextBuilder message) {
      send(message.build());
   }


   public static MutableComponent component(String text, ChatFormatting... formats) {
      return Component.literal(text).withStyle(formats);
   }

   public static MutableComponent create(Component base) {
      return PREFIX.copy().append(base);
   }

   public static String strip(String input) {
      if (input == null) return null;

      return STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
   }

   public static String strip(Component component) {
      if (component == null) return null;

      return strip(component.getString());
   }

   public static StringBuilder with(ChatFormatting... formattings) {
      StringBuilder builder = new StringBuilder();

      for (ChatFormatting formatting : formattings) {
         builder.append(COLOR_CHAR).append(formatting.getChar());
      }

      return builder;
   }

   @SuppressWarnings("DataFlowIssue")
   public static Color colorOf(ChatFormatting formatting) {
      return Color.ofOpaque(formatting.getColor());
   }

   public static StyledText toStyledText(FormattedText ft) {
      final StyledText[] styled = {StyledText.EMPTY};

      ft.visit((style, string) -> {
         styled[0] = styled[0].appendPart(new StyledTextPart(string, style, null, null));

         return Optional.empty();
      }, Style.EMPTY);

      return styled[0];
   }

   public static boolean equals(@Nullable StyledText text,  @Nullable String string) {
      return equals(text, string, PartStyle.StyleType.NONE);
   }

   public static boolean equals(@Nullable StyledText text, @Nullable String string, PartStyle.StyleType style) {
      if (text == null || string == null) return string == null && text == null;

      return text.equalsString(string, style);
   }

   public static boolean equals(@Nullable Component text,  @Nullable String string) {
      return equals(text, string, PartStyle.StyleType.NONE);
   }

   public static boolean equals(@Nullable Component text, @Nullable String string, PartStyle.StyleType style) {
      if (text == null || string == null) return string == null && text == null;

      return StyledText.fromComponent(text).equalsString(string, style);
   }
}
