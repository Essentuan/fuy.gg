package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(String.class)
public class StringCodec extends Codec<String, String> {
   @Override
   public @Nullable String write(String value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public @Nullable String read(@NotNull String value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public String fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return string;
   }

   @Override
   public String toString(String value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }
}
