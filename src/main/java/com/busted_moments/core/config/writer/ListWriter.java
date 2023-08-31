package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@Config.Writer(List.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ListWriter extends Writer<List, List> {
   @Override
   public @Nullable List write(List safe, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> listType = (Class<?>) typeArgs[0];

      Writer Writer = get(listType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) safe.stream().map(o -> {
         try {
            return Writer.write(o, listType, args);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public @Nullable List read(@NotNull List unsafe, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> listType = (Class<?>) typeArgs[0];

      Writer Writer = get(listType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) unsafe.stream().map(o -> {
         try {
            return Writer.read(o, listType, args);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public List fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(List value, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }
}
