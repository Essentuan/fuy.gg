package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.Codec;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Codec.Definition(JsonTemplate.class)
public class JsonTemplateCodec extends Codec<JsonTemplate, Json> {
   @Override
   public @Nullable Json write(JsonTemplate value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toJson();
   }

   @Override
   @SuppressWarnings("unchecked")
   public @Nullable JsonTemplate read(@NotNull Json value, Class<?> type, Type... typeArgs) throws Exception {
      return value.wrap((Class<? extends JsonTemplate>) type);
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
