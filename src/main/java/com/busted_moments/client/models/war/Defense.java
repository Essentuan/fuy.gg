package com.busted_moments.client.models.war;

import com.busted_moments.client.util.ChatUtil;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.StyledText;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.services.map.pois.TerritoryPoi;
import net.minecraft.ChatFormatting;

import static net.minecraft.ChatFormatting.*;

public enum Defense {
   VERY_HIGH("Very High", DARK_RED),
   HIGH("High", RED),
   MEDIUM("Medium", YELLOW),
   LOW("Low", GREEN),
   VERY_LOW("Very Low", DARK_GREEN),
   UNKNOWN("Unknown", GRAY);

   private final String WYNN_STRING;
   private final ChatFormatting[] formats;
   Defense(String string, ChatFormatting... formats) {
      this.WYNN_STRING = string;
      this.formats = formats;
   }

   public String toText(boolean confident) {
      StringBuilder builder = ChatUtil.with(formats);
      if (!confident) builder.append(ITALIC);

      return builder.append(WYNN_STRING).toString();
   }

   public static Defense from(String string) {
      for (Defense defense : values()) if (defense.WYNN_STRING.equals(string)) return defense;

      return UNKNOWN;
   }

   public static Defense from(GuildResourceValues values) {
      return switch(values) {
         case VERY_LOW -> VERY_LOW;
         case LOW -> LOW;
         case MEDIUM -> MEDIUM;
         case HIGH -> HIGH;
         case VERY_HIGH -> VERY_HIGH;
      };
   }

   public static Defense get(String territory) {
      TerritoryPoi poi = Models.Territory.getTerritoryPoiFromAdvancement(territory);
      if (poi == null) return UNKNOWN;

      return from(poi.getTerritoryInfo().getDefences());
   }
}
