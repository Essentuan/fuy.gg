package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.json.Json;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(Json.class)
public class JsonWriter extends Writer<Json, String> {
   @Override
   public @Nullable String write(Json value, Class<?> type, Type... typeArgs) {
      return toString(value, type, typeArgs);
   }

   @Override
   public @Nullable Json read(@NotNull String value, Class<?> type, Type... typeArgs) {
      return fromString(value, type, typeArgs);
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
