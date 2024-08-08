package com.busted_moments.mixin.invoker;

import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.map.pois.TerritoryPoi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Supplier;

@Mixin(value = TerritoryPoi.class, remap = false)
public interface TerritoryPoiInvoker {
   @Invoker("<init>")
   static TerritoryPoi create(
           Supplier<TerritoryProfile> territoryProfileSupplier,
           TerritoryInfo territoryInfo,
           boolean fakeTerritoryInfo
   ) {
      throw new AssertionError();
   }
}
