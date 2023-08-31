package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Codec.Definition(List.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public class ListCodec extends Codec<List, List> {
   @Override
   public @Nullable List write(List safe, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> listType = (Class<?>) typeArgs[0];

      Codec codec = get(listType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) safe.stream().map(o -> {
         try {
            return codec.write(o, listType, args);
         } catch (Exception e) {
            throw new RuntimeException(e);
         }
      }).collect(Collectors.toList());
   }

   @Override
   public @Nullable List read(@NotNull List unsafe, Class<?> type, Type... typeArgs) throws Exception {
      Class<?> listType = (Class<?>) typeArgs[0];

      Codec codec = get(listType);
      Type[] args = getTypeArgs(typeArgs[0]);

      return (List) unsafe.stream().map(o -> {
         try {
            return codec.read(o, listType, args);
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
