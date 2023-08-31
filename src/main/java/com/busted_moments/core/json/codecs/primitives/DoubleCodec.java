package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.json.codecs.EnumCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(Double.class)
public class DoubleCodec extends NumberCodec<Double> {
   @Override
   public @Nullable Double read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return value.doubleValue();
   }

   @Override
   public Double fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Double.valueOf(string);
   }
}
