package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.json.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(Json.class)
public class JsonCodec extends AbstractCodec<Json, Json> {
   @Override
   public @Nullable Json write(Json value, Class<?> type, Annotations annotations, Type... typeArgs) {
      return value;
   }

   @Override
   public @Nullable Json read(@NotNull Json value, Class<?> type, Annotations annotations, Type... typeArgs) {
      return value;
   }

   @Override
   public Json fromString(String string, Class<?> type, Type... typeArgs) {
      return Json.parse(string);
   }

   @Override
   public String toString(Json value, Class<?> type, Type... typeArgs) {
      return value.toString();
   }
}
