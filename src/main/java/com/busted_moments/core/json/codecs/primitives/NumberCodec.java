package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

abstract class NumberCodec<T extends Number> extends Codec<T, Number> {
   @Override
   public @Nullable Number write(T value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public String toString(T value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toString();
   }
}
