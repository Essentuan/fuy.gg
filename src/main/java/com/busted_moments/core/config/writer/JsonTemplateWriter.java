package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(JsonTemplate.class)
public class JsonTemplateWriter extends Writer<JsonTemplate, String> {
   @Override
   public @Nullable String write(JsonTemplate value, Class<?> type, Type... typeArgs) throws Exception {
      return toString(value, type, typeArgs);
   }

   @Override
   public @Nullable JsonTemplate read(@NotNull String value, Class<?> type, Type... typeArgs) throws Exception {
      return fromString(value, type, typeArgs);
   }

   @Override
   @SuppressWarnings("unchecked")
   public JsonTemplate fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Json.parse(string).wrap((Class<? extends JsonTemplate>) type);
   }

   @Override
   public String toString(JsonTemplate value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toJson().toString();
   }
}
