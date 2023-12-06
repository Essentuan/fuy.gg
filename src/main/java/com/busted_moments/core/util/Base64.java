package com.busted_moments.core.util;

import static java.util.Base64.getDecoder;
import static java.util.Base64.getEncoder;

public class Base64 {
   public static String encode(String string) {
      return getEncoder().encodeToString(string.getBytes());
   }

   public static String decode(String string) {
      return new String(getDecoder().decode(string.getBytes()));
   }
}
