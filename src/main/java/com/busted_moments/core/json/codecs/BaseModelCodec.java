package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.util.Priority;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(value = BaseModel.class, priority = Priority.NORMAL)
public class BaseModelCodec extends AbstractCodec<BaseModel, Json> {
   @Override
   public @Nullable Json write(BaseModel value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.toJson();
   }

   @Override
   @SuppressWarnings("unchecked")
   public @Nullable BaseModel read(@NotNull Json value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.wrap((Class<? extends BaseModel>) type);
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
