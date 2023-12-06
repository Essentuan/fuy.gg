package com.busted_moments.core.http.api.guild;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.util.Priority;
import com.busted_moments.core.util.Reflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(value = GuildType.class, priority = Priority.LOWEST)
public class GuildTypeCodec extends AbstractCodec<GuildType, Object> {
   @Override
   public @Nullable Object write(GuildType value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return Json.of("name", value.name())
              .set("prefix", value.prefix());
   }

   @Override
   public @Nullable GuildType read(@NotNull Object value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      if (value instanceof Json json)
         return GuildType.valueOf(json.getString("name"), json.getString("prefix"));
      else if (value instanceof String string)
         return Guild.valueOf(string);
      else if (value instanceof GuildType guild)
         return guild;
      else throw new IllegalArgumentException("%s cannot be cast to GuildType!".formatted(Reflection.toSimpleString(value.getClass())));
   }

   @Override
   public GuildType fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Guild.valueOf(string);
   }

   @Override
   public String toString(GuildType value, Class<?> type, Type... typeArgs) throws Exception {
      return value.name();
   }
}
