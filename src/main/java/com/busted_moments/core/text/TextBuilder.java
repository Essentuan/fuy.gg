package com.busted_moments.core.text;

import com.wynntils.core.text.StyledText;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TextBuilder {
   TextBuilder append(StyledText text);

   TextBuilder append(Component text);

   default TextBuilder append(Component text, ChatFormatting... styles) {
      return append(text.getString(), styles);
   }

   TextBuilder append(String string, ChatFormatting... styles);

   default TextBuilder append(Object string, ChatFormatting... styles) {
      return append(String.valueOf(string), styles);
   }

   default <T> TextBuilder append(Iterable<T> iterable, BiConsumer<T, TextBuilder> consumer) {
      for (var iter = iterable.iterator(); iter.hasNext(); ) {
         consumer.accept(iter.next(), this);
         if (iter.hasNext()) line();
      }

      return this;
   }

   default <T> TextBuilder append(Iterable<T> iterable, Consumer<T> consumer) {
      for (var iter = iterable.iterator(); iter.hasNext(); ) {
         consumer.accept(iter.next());
         if (iter.hasNext()) line();
      }

      return this;
   }

   default TextBuilder appendIf(Supplier<Boolean> condition, String string, ChatFormatting... styles) {
      if (condition.get()) append(string, styles);

      return this;
   }

   TextBuilder prepend(StyledText text);

   TextBuilder prepend(Component text);

   TextBuilder prepend(String string, ChatFormatting... styles);

   default TextBuilder prepend(Object string, ChatFormatting... styles) {
      return prepend(String.valueOf(string), styles);
   }

   TextBuilder onHover(HoverEvent event);

   default <T> TextBuilder onHover(HoverEvent.Action<T> action, T obj) {
      return onHover(new HoverEvent(action, obj));
   };

   TextBuilder onPartHover(HoverEvent event);

   default <T> TextBuilder onPartHover(HoverEvent.Action<T> action, T obj) {
      return onPartHover(new HoverEvent(action, obj));
   };

   TextBuilder onClick(ClickEvent event);

   default TextBuilder onClick(ClickEvent.Action action, String value) {
      return onClick(new ClickEvent(action, value));
   }

   TextBuilder onPartClick(ClickEvent event);

   default TextBuilder onPartClick(ClickEvent.Action action, String value) {
      return onPartClick(new ClickEvent(action, value));
   }


   TextBuilder next();

   TextBuilder line();

   TextBuilder space();

   StyledText build();

   default Component toComponent() {
      return build().getComponent();
   }

   static TextBuilder empty() {
      return new TextBuilderImpl();
   }

   static TextBuilder of(String string, ChatFormatting... formattings) {
      return empty().append(string, formattings);
   }

   static TextBuilder of(StyledText string) {
      return new TextBuilderImpl(string);
   }

   static TextBuilder of(Component string) {
      return new TextBuilderImpl(StyledText.fromComponent(string));
   }
}
