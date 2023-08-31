package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Date;

@Config.Writer(Date.class)
public class DateWriter extends Writer<Date, Number> {
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
