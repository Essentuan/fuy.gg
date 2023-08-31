package com.busted_moments.core.artemis;

import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.features.Feature;
import com.wynntils.core.consumers.features.FeatureManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ArtemisFeatures {
   private static Method REGISTER_METHOD;

   public static void register(Feature feature) {
      if (REGISTER_METHOD == null) getMethod();

      try {
         REGISTER_METHOD.invoke(Managers.Feature, feature);
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }

   private static void getMethod() {
      try {
         REGISTER_METHOD = FeatureManager.class.getDeclaredMethod("registerFeature", Feature.class);
         REGISTER_METHOD.setAccessible(true);
      } catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
   }
}
