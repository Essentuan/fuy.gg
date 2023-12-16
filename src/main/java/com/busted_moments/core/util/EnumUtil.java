package com.busted_moments.core.util;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class EnumUtil {
   public static final Pattern EMPTY_PATTERN = Pattern.compile("[_ -']");

   @SuppressWarnings("unchecked")
   public static <T extends Enum<T>> Optional<T> valueOf(String string, Class<T> cls) {
      try {
         return Optional.of(Enum.valueOf(cls, string));
      } catch (Exception e) {
         return StringUtil.bestMatch(
                 fix(string),
                 List.of((Enum<T>[]) cls.getEnumConstants()),
                 EnumUtil::fix,
                 false
         ).map(v -> (T) v);
      }
   }

   public static String fix(Object object) {
      return EMPTY_PATTERN.matcher(object.toString()).replaceAll("");
   }

   public static <T extends Enum<T>> Set<T> asSet(T[] array, Class<T> cls) {
      if (array.length == 0)
         return EnumSet.noneOf(cls);

      return EnumSet.of(array[0], array);
   }
}
