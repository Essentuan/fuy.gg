package com.busted_moments.client.util;

public class PlayerUtil {
   public static boolean isPlayer(String username) {
      return username.matches("[a-zA-Z0-9_]+");
   }
}
