package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import com.busted_moments.core.toml.Toml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(JsonTemplate.class)
public class JsonTemplateWriter extends Writer<JsonTemplate, Toml> {
   @Override
   public @Nullable Toml write(JsonTemplate value, Class<?> type, Type... typeArgs) throws Exception {
      return Toml.of(value.toJson());
   }

   @Override
   @SuppressWarnings("unchecked")
   public @Nullable JsonTemplate read(@NotNull Toml value, Class<?> type, Type... typeArgs) throws Exception {
      return Json.of(value).wrap((Class<? extends JsonTemplate>) type);
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
