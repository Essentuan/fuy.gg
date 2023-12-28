package com.busted_moments.core.text;

import com.busted_moments.client.util.ChatUtil;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

class TextBuilderImpl implements TextBuilder {
   protected static final Field parts;
   protected static final Method ADD_CLICK_EVENT;
   protected static final Method ADD_HOVER_EVENT;

   static {
      try {
         parts = StyledText.class.getDeclaredField("parts");
         parts.setAccessible(true);

         ADD_CLICK_EVENT = StyledText.class.getDeclaredMethod("addClickEvent", ClickEvent.class);
         ADD_CLICK_EVENT.setAccessible(true);

         ADD_HOVER_EVENT = StyledText.class.getDeclaredMethod("addHoverEvent", HoverEvent.class);
         ADD_HOVER_EVENT.setAccessible(true);
      } catch (NoSuchFieldException | NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }

   private StyledText result = StyledText.EMPTY;

   private StyledText current;

   public TextBuilderImpl() {
      this(StyledText.EMPTY);
   }

   public TextBuilderImpl(StyledText text) {
      current = text;
   }

   @Override
   public TextBuilder append(StyledText text) {
      current = current.append(text);

      return this;
   }

   @Override
   public TextBuilder append(Component text) {
      current = current.append(StyledText.fromComponent(text));

      return this;
   }

   @Override
   public TextBuilder append(StyledTextPart part) {
      return append(StyledText.fromPart(part));
   }

   @Override
   public TextBuilder append(String string, ChatFormatting... styles) {
      current = current.append(ChatUtil.with(styles).append(string).toString());

      return this;
   }

   @Override
   public TextBuilder prepend(StyledText text) {
      current = current.prepend(text);

      return this;
   }

   @Override
   public TextBuilder prepend(Component text) {
      current = current.prepend(StyledText.fromComponent(text));

      return this;
   }

   @Override
   public TextBuilder prepend(String string, ChatFormatting... styles) {
      current = current.prepend(string + ChatUtil.with(styles));

      return this;
   }

   private TextBuilder replace(Function<PartStyle, PartStyle> style) {
      List<StyledTextPart> parts = getParts(current);
      if (parts.isEmpty()) return this;

      for (var iter = parts.listIterator(); iter.hasNext(); ) iter.set(iter.next().withStyle(style));
      return this;
   }

   @Override
   public TextBuilder onHover(HoverEvent event) {
      return replace(style -> style.withHoverEvent(event));
   }

   @Override
   public TextBuilder onPartHover(HoverEvent event) {
      List<StyledTextPart> parts = getParts(current);
      if (parts.isEmpty()) return this;

      StyledTextPart last = current.getLastPart();

      parts.set(parts.size() - 1, last.withStyle(style -> style.withHoverEvent(event)));

      return this;
   }

   @Override
   public TextBuilder onClick(ClickEvent event) {
      return replace(style -> style.withClickEvent(event));
   }

   @Override
   public TextBuilder onPartClick(ClickEvent event) {
      List<StyledTextPart> parts = getParts(current);
      if (parts.isEmpty()) return this;

      StyledTextPart last = current.getLastPart();

      parts.set(parts.size() - 1, last.withStyle(style -> style.withClickEvent(event)));

      return this;
   }

   @Override
   public TextBuilder next() {
      result = result.append(current);

      current = StyledText.EMPTY;

      return this;
   }

   @Override
   public TextBuilder line() {
      append("\n");

      return next();
   }

   @Override
   public TextBuilder space() {
      return append(" ");
   }

   @Override
   public StyledText build() {
      if (current.length() > 1) next();

      return result;
   }

   @SuppressWarnings("unchecked")
   private static List<StyledTextPart> getParts(StyledText text) {
      try {
         return (List<StyledTextPart>) parts.get(text);
      } catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
   }

   private static void addClickEvent(StyledText text, ClickEvent event) {
      try {
         ADD_CLICK_EVENT.invoke(text, event);
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }

   private static void addHoverEvent(StyledText text, HoverEvent event) {
      try {
         ADD_HOVER_EVENT.invoke(text, event);
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }
}
