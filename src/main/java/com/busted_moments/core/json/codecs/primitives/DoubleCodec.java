package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(Double.class)
public class DoubleCodec extends NumberCodec<Double> {
   @Override
   public @Nullable Double read(@NotNull Number value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.doubleValue();
   }

   @Override
   public Double fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Double.valueOf(string);
   }
}
