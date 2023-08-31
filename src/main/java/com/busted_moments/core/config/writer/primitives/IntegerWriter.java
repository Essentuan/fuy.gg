package com.busted_moments.core.config.writer.primitives;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(Integer.class)
public class IntegerWriter extends NumberWriter<Integer> {

   @Override
   public @Nullable Integer read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return value.intValue();
   }

   @Override
   public Integer fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Integer.valueOf(string);
   }
}
