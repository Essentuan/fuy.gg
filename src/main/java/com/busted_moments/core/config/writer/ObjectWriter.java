package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.util.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(value = Object.class, priority = Priority.LOWEST)
public class ObjectWriter extends Writer<Object, Object> {
   @Nullable
   @Override
   public Object write(Object value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public @NotNull Object read(@NotNull Object value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public Object fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(Object value, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }
};
