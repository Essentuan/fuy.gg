package com.busted_moments.core.config.writer;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.config.Writer;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.toml.Toml;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@Config.Writer(BaseModel.class)
public class BaseModelWriter extends Writer<BaseModel, Toml> {
   @Override
   public @Nullable Toml write(BaseModel value, Class<?> type, Type... typeArgs) throws Exception {
      return Toml.of(value.toJson());
   }

   @Override
   @SuppressWarnings("unchecked")
   public @Nullable BaseModel read(@NotNull Toml value, Class<?> type, Type... typeArgs) throws Exception {
      return Json.of(value).wrap((Class<? extends BaseModel>) type);
   }

   @Override
   @SuppressWarnings("unchecked")
   public BaseModel fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Json.parse(string).wrap((Class<? extends BaseModel>) type);
   }

   @Override
   public String toString(BaseModel value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toJson().toString();
   }
}
