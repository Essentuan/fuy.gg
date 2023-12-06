package com.busted_moments.core.json.codecs;

import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.URI;
import java.net.URL;

@AbstractCodec.Definition(URL.class)
public class URLCodec extends AbstractCodec<URL, String> {
   @Override
   public @Nullable String write(URL value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return value.toString();
   }

   @Override
   public @Nullable URL read(@NotNull String value, Class<?> type, Annotations annotations, Type... typeArgs) throws Exception {
      return new URI(value).toURL();
   }

   @Override
   public URL fromString(String string, Class<?> type, Type... typeArgs) throws Exception {
      return new URI(string).toURL();
   }

   @Override
   public String toString(URL value, Class<?> type, Type... typeArgs) throws Exception {
      return value.toString();
   }
}
