package com.busted_moments.core.json.codecs;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.json.AbstractCodec;
import com.busted_moments.core.json.Annotations;
import com.busted_moments.core.util.Reflection;
import com.busted_moments.core.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Pattern;

@SuppressWarnings({"rawtypes"})
@AbstractCodec.Definition(Enum.class)
public class EnumCodec extends AbstractCodec<Enum, String> {
   @Override
   public @Nullable String write(Enum value, Class<?> type, Annotations annotations, Type... typeArgs) {
      return toString(value, type, typeArgs);
   }

   @Override
   public @Nullable Enum read(@NotNull String value, Class<?> type, Annotations annotations, Type... typeArgs) {
      return fromString(value, type, typeArgs);
   }

   public static final Pattern EMPTY_PATTERN = Pattern.compile("[_ -']");

   @Override
   public Enum fromString(String string, Class<?> type, Type... typeArgs) {
      try {
         return Enum.valueOf((Class<? extends Enum>) type, string);
      } catch (Exception e) {
         return StringUtil.bestMatch(
                 fix(string),
                 List.of((Enum[]) type.getEnumConstants()),
                 EnumCodec::fix,
                 false
         ).orElseThrow(() -> new UnexpectedException("Couldn't find %s that matches %s", Reflection.toSimpleString(type), string));
      }
   }

   @Override
   public String toString(Enum value, Class<?> type, Type... typeArgs) {
      return value.toString();
   }

   public static String fix(Object object) {
      return EMPTY_PATTERN.matcher(object.toString()).replaceAll("");
   }
}
