package com.busted_moments.core.config.writer.primitives;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(Float.class)
public class FloatWriter extends NumberWriter<Float> {
   @Override
   public @Nullable Float read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return value.floatValue();
   }

   @Override
   public Float fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Float.valueOf(string);
   }
}
