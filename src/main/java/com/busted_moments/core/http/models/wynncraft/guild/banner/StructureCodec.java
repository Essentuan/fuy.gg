package com.busted_moments.core.http.models.wynncraft.guild.banner;

import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;

@AbstractCodec.Definition(Guild.Banner.Structure.class)
public class StructureCodec extends AbstractCodec<Guild.Banner.Structure, String> {
   @Override
   public @Nullable String write(Guild.Banner.Structure value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.toString();
   }

   @Override
   public @Nullable Guild.Banner.Structure read(@NotNull String value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return Guild.Banner.Structure.of(value);
   }

   @Override
   public Guild.Banner.Structure fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return Guild.Banner.Structure.of(string);
   }

   @Override
   public String toString(Guild.Banner.Structure value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toString();
   }
}
