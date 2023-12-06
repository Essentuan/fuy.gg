package com.busted_moments.client.features.war;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.FormatFlag;
import com.busted_moments.core.time.ChronoUnit;

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
}
