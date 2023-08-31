package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(Float.class)
public class FloatCodec extends NumberCodec<Float> {
   @Override
   public @Nullable Float read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return value.floatValue();
   }

   @Override
   public Float fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Float.valueOf(string);
   }
}
