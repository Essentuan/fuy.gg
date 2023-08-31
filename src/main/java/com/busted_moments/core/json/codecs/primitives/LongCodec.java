package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(Long.class)
public class LongCodec extends NumberCodec<Long> {
   @Override
   public @Nullable Long read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return value.longValue();
   }

   @Override
   public Long fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Long.valueOf(string);
   }
}
