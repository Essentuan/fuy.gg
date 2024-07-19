package com.busted_moments.mixin.extensions;

import com.busted_moments.client.framework.marker.Marker;
import com.wynntils.models.marker.type.MarkerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = MarkerInfo.class, remap = false)
public abstract class MarkerInfoExtension implements Marker.Extension {
   @Unique
   private boolean label = false;

   @Override
   public boolean getHasLabel() {
      return label;
   }

   @Override
   public void setHasLabel(boolean b) {
      label = b;
   }
}
