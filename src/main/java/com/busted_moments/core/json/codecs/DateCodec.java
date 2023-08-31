package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.json.template.EntryWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Date;

@Codec.Definition(Date.class)
public class DateCodec extends Codec<Date, Number> {
   @Override
   public @Nullable Long write(Date value, Class<?> type, Type... typeArgs) {
      return value.getTime();
   }

   @Override
   public @Nullable Date read(@NotNull Number value, Class<?> type, Type... typeArgs) {
      return new Date(value.longValue());
   }

   @Override
   public Date fromString(String string, Class<?> type, Type... typeArgs) {
      return new Date(Long.parseLong(string));
   }

   @Override
   public String toString(Date value, Class<?> type, Type... typeArgs) {
      return Long.toString(value.getTime());
   }
}
