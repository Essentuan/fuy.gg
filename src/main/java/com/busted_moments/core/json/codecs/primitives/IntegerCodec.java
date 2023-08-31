package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(Integer.class)
public class IntegerCodec extends NumberCodec<Integer> {

   @Override
   public @Nullable Integer read(@NotNull Number value, Class<?> type, Type... typeArgs) throws Exception {
      return value.intValue();
   }

   @Override
   public Integer fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Integer.valueOf(string);
   }
}
