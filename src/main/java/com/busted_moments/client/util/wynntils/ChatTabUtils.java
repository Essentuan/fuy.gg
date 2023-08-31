package com.busted_moments.client.util.wynntils;

import com.wynntils.core.components.Managers;
import com.wynntils.features.chat.ChatTabsFeature;

public class ChatTabUtils {
   private static ChatTabsFeature feature = null;

   public static ChatTabsFeature getFeature() {
      if (feature == null) feature = Managers.Feature.getFeatureInstance(ChatTabsFeature.class);
      return feature;
   }

   public static boolean isEnabled() {
      return getFeature().isEnabled();
   }
}
