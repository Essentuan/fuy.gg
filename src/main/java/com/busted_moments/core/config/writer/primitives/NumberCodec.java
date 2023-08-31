package com.busted_moments.core.config.writer.primitives;

import com.busted_moments.core.config.Writer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

abstract class NumberWriter<T extends Number> extends Writer<T, Number> {
   @Override
   public @Nullable Number write(T value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public String toString(T value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toString();
   }
}
