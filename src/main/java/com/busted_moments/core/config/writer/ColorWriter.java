package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import me.shedaniel.math.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(Color.class)
public class ColorWriter extends Writer<Color, Number> {
   @Override
   public @Nullable Number write(Color value, Class<?> type, Type... typeArgs) throws Exception {
      return value.getColor();
   }

   @Override
   public @Nullable Color read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return Color.ofTransparent(value.intValue());
   }

   @Override
   public Color fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Color.ofTransparent(Integer.parseInt(string));
   }

   @Override
   public String toString(Color value, Class<?> type, Type... typeArgs) throws Exception {
      return Integer.toString(value.getColor());
   }
}
