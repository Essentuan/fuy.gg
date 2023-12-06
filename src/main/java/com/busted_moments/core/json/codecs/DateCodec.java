package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.util.DateUtil;
import com.busted_moments.core.util.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Date;

@AbstractCodec.Definition(Date.class)
public class DateCodec extends AbstractCodec<Date, Object> {
   @Override
   public @Nullable Long write(Date value, Class<?> type, Annotations annotations, Type... typeArgs) {
      return value.getTime();
   }

   @Override
   public @Nullable Date read(@NotNull Object value, Class<?> type, Annotations annotations, Type... typeArgs) {
      if (value instanceof Number number)
         return new Date(number.longValue());
      else if (value instanceof Date date)
         return date;
      else if (value instanceof String string)
         return DateUtil.fromISOString(string);
      else
         throw new IllegalArgumentException("Cannot cast %s to Date".formatted(
                 Reflection.toSimpleString(value.getClass())
         ));
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
