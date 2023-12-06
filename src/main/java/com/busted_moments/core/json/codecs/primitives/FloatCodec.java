package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(Float.class)
public class FloatCodec extends NumberCodec<Float> {
   @Override
   public @Nullable Float read(@NotNull Number value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.floatValue();
   }

   @Override
   public Float fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Float.valueOf(string);
   }
}
