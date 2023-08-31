package com.busted_moments.core.util;

import java.util.UUID;

public class UUIDUtil {
   public static String fromMojangUUID(String uuid) {
      return uuid.replaceFirst(
              "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)", "$1-$2-$3-$4-$5"
      );
   }

   public static boolean isUUID(String string) {
      try{
         UUID.fromString(string);
         return true;
      } catch (Exception ignored) {}

      return isMojangUUID(string);
   }

   public static boolean isMojangUUID(String string) {
      try {
         UUID uuid = UUID.fromString(fromMojangUUID(string));

         return true;
      } catch(Exception ignored) {
         return false;
      }
   }

   public static UUID parseUUID(String uuid) {
      if (isMojangUUID(uuid)) {
         return UUID.fromString(fromMojangUUID(uuid));
      }

      if (isUUID(uuid)) {
         return UUID.fromString(uuid);
      }

      return null;
   }
}
