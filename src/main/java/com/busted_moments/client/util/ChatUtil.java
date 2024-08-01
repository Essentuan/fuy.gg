package com.busted_moments.client.util;

import com.busted_moments.client.util.wynntils.ChatTabUtils;
import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.tuples.Pair;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.features.chat.ChatCoordinatesFeature;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.MessageType;
import com.wynntils.handlers.chat.type.RecipientType;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.wynntils.utils.mc.McUtils.mc;
import static com.wynntils.utils.mc.McUtils.sendMessageToClient;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.BLUE;
import static net.minecraft.ChatFormatting.DARK_GREEN;
import static net.minecraft.ChatFormatting.GOLD;
import static net.minecraft.ChatFormatting.GREEN;
import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.RESET;
import static net.minecraft.ChatFormatting.WHITE;
import static net.minecraft.ChatFormatting.YELLOW;

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
   private static final int PREFIX_LENGTH = StyledText.fromComponent(PREFIX).length();

   public static final char COLOR_CHAR = '§';
   public static final Pattern COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORr]");

   public static MutableComponent create(String text, ChatFormatting... formats) {
      return create(component(text, formats));
   }

   public static void message(String message, ChatFormatting... formats) {
      message(component(message, formats));
   }

   public static void message(String message, Object... args) {
      message(TextBuilder.empty().append(message, args));
   }

   public static void message(String message) {
      message(component(message, WHITE));
   }


   public static void message(TextBuilder message) {
      message(message.build());
   }


   public static void message(StyledText message) {
      if (mc().player == null) return;

      FontRenderer.split(message, 0).forEach(text ->
              sendMessageToClient(text.isBlank() ? Component.empty() : create(text.getComponent())));
   }


   public static void message(Component message) {
      message(StyledText.fromComponent(message));
   }

   public static void send(Component component) {
      send(ChatUtil.toStyledText(component), component);
   }

   private static List<Consumer<ChatMessageReceivedEvent>> HANDLERS;

   //This is purely due to chat coordinates messing with text (mainly in TowerStatsFeature) [and I don't like this solution]
   private static void process(ChatMessageReceivedEvent event) {
      if (HANDLERS == null)
         HANDLERS = Managers.Feature.getFeatures().stream()
                 .flatMap(feature ->
                         feature instanceof ChatCoordinatesFeature ?
                                 Stream.empty() :
                                 Stream.of(feature.getClass().getDeclaredMethods())
                                         .filter(m -> m.isAnnotationPresent(SubscribeEvent.class) &&
                                                 m.getParameterCount() == 1 &&
                                                 m.getParameterTypes()[0] == ChatMessageReceivedEvent.class)
                                         .map(m -> Pair.of(feature, m)))
                 .sorted(Comparator.comparing(pair -> pair.two().getAnnotation(SubscribeEvent.class).priority()))
                 .map(pair -> {
                    pair.two().setAccessible(true);

                    return (Consumer<ChatMessageReceivedEvent>) e -> {
                       if (pair.one().isEnabled()) {
                          try {
                             pair.two().invoke(pair.one(), e);
                          } catch (IllegalAccessException | InvocationTargetException ex) {
                             throw new RuntimeException(ex);
                          }
                       }
                    };
                 }).toList();

      HANDLERS.forEach(consumer -> consumer.accept(event));
   }

   private static void send(StyledText text, Component component) {
      var event = new ChatMessageReceivedEvent(text, MessageType.FOREGROUND, RecipientType.CLIENTSIDE);
      process(event);

      if (!ChatTabUtils.isEnabled()) mc().gui.getChat().addMessage(event.getStyledText().getComponent());
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

   public static void send(String message, Object... args) {
      send(TextBuilder.empty().append(message, args));
   }


   public static MutableComponent component(String text, ChatFormatting... formats) {
      return Component.literal(text).withStyle(formats);
   }

   public static MutableComponent create(Component base) {
      return PREFIX.copy().append(base);
   }

   public static String strip(String input) {
      if (input == null) return null;

      return COLOR_PATTERN.matcher(input).replaceAll("");
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

   public static boolean equals(@Nullable StyledText text, @Nullable String string) {
      return equals(text, string, PartStyle.StyleType.NONE);
   }

   public static boolean equals(@Nullable StyledText text, @Nullable String string, PartStyle.StyleType style) {
      if (text == null || string == null) return string == null && text == null;

      return text.equalsString(string, style);
   }

   public static boolean equals(@Nullable Component text, @Nullable String string) {
      return equals(text, string, PartStyle.StyleType.NONE);
   }

   public static boolean equals(@Nullable Component text, @Nullable String string, PartStyle.StyleType style) {
      if (text == null || string == null) return string == null && text == null;

      return StyledText.fromComponent(text).equalsString(string, style);
   }

   public static int prefixLength() {
      return PREFIX_LENGTH;
   }
}
