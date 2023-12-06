package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(Boolean.class)
public class BooleanCodec extends AbstractCodec<Boolean, Boolean> {
   @Override
   public @Nullable Boolean write(Boolean value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public @Nullable Boolean read(@NotNull Boolean value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public Boolean fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Boolean.valueOf(string);
   }

   @Override
   public String toString(Boolean value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toString();
   }
}
