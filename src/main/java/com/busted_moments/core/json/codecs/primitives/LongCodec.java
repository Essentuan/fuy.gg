package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(Long.class)
public class LongCodec extends NumberCodec<Long> {
   @Override
   public @Nullable Long read(@NotNull Number value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.longValue();
   }

   @Override
   public Long fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Long.valueOf(string);
   }
}
