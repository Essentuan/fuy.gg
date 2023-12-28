package com.busted_moments.core.text;

import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface TextBuilder {
   TextBuilder append(StyledText text);

   TextBuilder append(Component text);

   TextBuilder append(StyledTextPart part);

   default TextBuilder append(Component text, ChatFormatting... styles) {
      return append(text.getString(), styles);
   }

   TextBuilder append(String string, ChatFormatting... styles);

   default TextBuilder append(String format, Object... args) {
      List<ChatFormatting> formattings = new ArrayList<>();

      return append(format.formatted(
              Stream.of(args).filter(obj -> {
                 if (obj instanceof ChatFormatting formatting) {
                    formattings.add(formatting);
                    return false;
                 } else return true;
              }).map(obj -> {
                 if (obj instanceof Component component)
                    return component.getString();
                 else if (obj instanceof StyledText text)
                    return text.getString();
                 else return obj;
              }).toArray()
      ), formattings.toArray(ChatFormatting[]::new));
   }

   default TextBuilder append(Object string, ChatFormatting... styles) {
      return append(String.valueOf(string), styles);
   }

   default <T> TextBuilder append(Iterable<T> iterable, BiConsumer<T, TextBuilder> consumer) {
      return append(iterable, consumer, TextBuilder::line);
   }

   default <T> TextBuilder append(Iterable<T> iterable, BiConsumer<T, TextBuilder> consumer, Consumer<TextBuilder> next) {
      for (var iter = iterable.iterator(); iter.hasNext(); ) {
         consumer.accept(iter.next(), this);
         if (iter.hasNext()) next.accept(this);
      }

      return this;
   }

   default <T> TextBuilder append(Iterable<T> iterable, Consumer<T> consumer) {
      return append(iterable, consumer, TextBuilder::line);
   }

   default <T> TextBuilder append(Iterable<T> iterable, Consumer<T> consumer, Consumer<TextBuilder> next) {
      for (var iter = iterable.iterator(); iter.hasNext(); ) {
         consumer.accept(iter.next());
         if (iter.hasNext()) next.accept(this);
      }

      return this;
   }

   default TextBuilder appendIf(boolean condition, String string, ChatFormatting... styles) {
      if (condition) append(string, styles);

      return this;
   }


   default TextBuilder appendIf(Supplier<Boolean> condition, String string, ChatFormatting... styles) {
      return appendIf(condition.get(), string, styles);
   }

   default TextBuilder appendIf(boolean condition, String format, Object... args) {
      if (condition) append(format, args);

      return this;
   }

   default TextBuilder appendIf(boolean condition, String format, Supplier<Object[]> args, ChatFormatting... styles) {
      if (condition) append(format.formatted(args.get()), styles);

      return this;
   }

   default TextBuilder appendIf(Supplier<Boolean> condition, String format, Object... args) {
      return appendIf(condition.get(), format, args);
   }

   default TextBuilder appendIf(Supplier<Boolean> condition, String format, Supplier<Object[]> args, ChatFormatting... styles) {
      return appendIf(condition.get(), format, args.get(), styles);
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
   }

   ;

   default TextBuilder onHover(Consumer<TextBuilder> consumer) {
      TextBuilder builder = TextBuilder.empty();
      consumer.accept(builder);

      return onHover(HoverEvent.Action.SHOW_TEXT, builder.toComponent());
   }

   ;

   TextBuilder onPartHover(HoverEvent event);

   default <T> TextBuilder onPartHover(HoverEvent.Action<T> action, T obj) {
      return onPartHover(new HoverEvent(action, obj));
   }

   ;

   default TextBuilder onPartHover(Consumer<TextBuilder> consumer) {
      TextBuilder builder = TextBuilder.empty();
      consumer.accept(builder);

      return onPartHover(HoverEvent.Action.SHOW_TEXT, builder.toComponent());
   }

   ;


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
