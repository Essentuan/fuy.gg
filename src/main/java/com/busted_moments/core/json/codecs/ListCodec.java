package com.busted_moments.core.json.codecs;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.json.BaseModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

@AbstractCodec.Definition(List.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ListCodec extends AbstractCodec<List, List> {
   @Override
   public @Nullable List write(List safe, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      Class<?> listType = getClass(typeArgs[0]);

      AbstractCodec codec = get(listType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) safe.stream().map(o -> {
         try {
            return codec.write(o, listType, Annotations.empty(), args);
         } catch (Exception e) {
            throw UnexpectedException.propagate(e);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public @Nullable List read(@NotNull List unsafe, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      Class<?> listType = getClass(typeArgs[0]);

      AbstractCodec codec = get(listType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) unsafe.stream().map(o -> {
         try {
            return codec.read(o, listType, Annotations.empty(), args);
         } catch (Exception e) {
            throw UnexpectedException.propagate(e);
         }
      }).collect(annotations.contains(BaseModel.Final.class) ? Collectors.toUnmodifiableList() : Collectors.toList());
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
