package com.busted_moments.core.util;

public class CharUtil {
   public static boolean equalsIgnoreCase(char c1, char c2) {
      return c1 == c2 || Character.toLowerCase(c1) == Character.toLowerCase(c2);
   }
}
