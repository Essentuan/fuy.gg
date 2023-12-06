package com.busted_moments.core.json.codecs;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.collector.LinkedSetCollector;
import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.json.BaseModel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@AbstractCodec.Definition(Set.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class SetCodec extends AbstractCodec<Set, List> {
   @Override
   public @Nullable List write(Set safe, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      Class<?> setType = getClass(typeArgs[0]);

      AbstractCodec codec = get(setType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) safe.stream().map(o -> {
         try {
            return codec.write(o, setType, Annotations.empty(), args);
         } catch (Exception e) {
            throw new InternalError(e);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public @Nullable Set read(@NotNull List unsafe, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      Class<?> setType = getClass(typeArgs[0]);

      AbstractCodec codec = get(setType);
      Type[] args = getTypeArgs(typeArgs[0]);

      Set finished = (Set) unsafe.stream().map(o -> {
         try {
            return codec.read(o, setType, Annotations.empty(), args);
         } catch (Exception e) {
            throw UnexpectedException.propagate(e);
         }
      }).collect(new LinkedSetCollector(Function.identity()));

      if (annotations.contains(BaseModel.Final.class))
         return Set.copyOf(finished);
      else
         return finished;
   }

   @Override
   public Set fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }

   @Override
   public String toString(Set value, Class<?> type, Type... typeArgs) throws Exception {
      throw new UnsupportedOperationException();
   }
}
