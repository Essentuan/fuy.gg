package com.busted_moments.core.json.codecs.primitives;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(String.class)
public class StringCodec extends AbstractCodec<String, Object> {
   @Override
   public @Nullable String write(String value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value;
   }

   @Override
   public @Nullable String read(@NotNull Object value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.toString();
   }

   @Override
   public String fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return string;
   }

   @Override
   public String toString(String value, Class<?> type, Type... typeArgs) throws Exception {
      return value;
   }
}
