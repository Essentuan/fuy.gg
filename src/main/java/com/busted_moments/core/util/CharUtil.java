package com.busted_moments.core.util;

import java.util.regex.Pattern;

public class CharUtil {
   private static final Pattern VOWELS = Pattern.compile("[aeiou]");

   public static boolean equalsIgnoreCase(char c1, char c2) {
      return c1 == c2 || Character.toLowerCase(c1) == Character.toLowerCase(c2);
   }

   public static boolean isVowel(char c) {
      return VOWELS.matcher(String.valueOf(c)).matches();
   }
}
