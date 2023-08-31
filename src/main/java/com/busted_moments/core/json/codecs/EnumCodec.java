package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@SuppressWarnings({"rawtypes", "unchecked"})
@Codec.Definition(Enum.class)
public class EnumCodec extends Codec<Enum, String> {
   @Override
   public @Nullable String write(Enum value, Class<?> type, Type... typeArgs) {
      return toString(value, type, typeArgs);
   }

   @Override
   public @Nullable Enum read(@NotNull String value, Class<?> type, Type... typeArgs) {
      return fromString(value, type, typeArgs);
   }

   @Override
   public Enum fromString(String string, Class<?> type, Type... typeArgs) {
      return Enum.valueOf((Class<? extends Enum>) type, string);
   }

   @Override
   public String toString(Enum value, Class<?> type, Type... typeArgs) {
      return value.toString();
   }
}
