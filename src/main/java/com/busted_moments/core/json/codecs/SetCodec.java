package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import org.apache.commons.lang3.NotImplementedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Codec.Definition(Set.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class SetCodec extends Codec<Set, List> {
   @Override
   public @Nullable List write(Set safe, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> setType = (Class<?>) typeArgs[0];

      Codec codec = get(setType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) safe.stream().map(o -> {
         try {
            return codec.write(o, setType, args);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public @Nullable Set read(@NotNull List unsafe, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> setType = (Class<?>) typeArgs[0];

      Codec codec = get(setType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (Set) unsafe.stream().map(o -> {
         try {
            return codec.read(o, setType, args);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }).collect(Collectors.toSet());
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
