package com.busted_moments.client.features.war;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.FormatFlag;
import com.busted_moments.core.time.ChronoUnit;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Config.Category("War")
public class WarCommon extends Config {
   @Value("Show seconds")
   @Tooltip("Shows seconds instead of a formatted time (1m 43s)")
   private static boolean USE_SECONDS = false;

   static String format(Duration duration) {
      if (duration.isForever()) return "Forever";
      else if (duration.lessThan(1, ChronoUnit.SECONDS)) return "0s";
      else if (USE_SECONDS) {
         int seconds = (int) duration.toSeconds();
         return seconds + " second" + (seconds == 1 ? "" : "s");
      }
      else return duration.toString(FormatFlag.COMPACT, ChronoUnit.SECONDS);
   }

   public static String getAcronym(TerritoryEco eco) {
      return Stream.of(eco.getName().split("[ \\-]"))
              .map(string -> string.isBlank() ? "" : String.valueOf(string.charAt(0)))
              .collect(Collectors.joining());
   }
}
