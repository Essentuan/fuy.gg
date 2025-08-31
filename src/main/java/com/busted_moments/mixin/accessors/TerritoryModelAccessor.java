package com.busted_moments.mixin.accessors;

import com.wynntils.models.territories.TerritoryModel;
import com.wynntils.models.territories.profile.TerritoryProfile;
import com.wynntils.services.map.pois.TerritoryPoi;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(value = TerritoryModel.class, remap = false)
public interface TerritoryModelAccessor {
    @Accessor()
    Map<String, TerritoryPoi> getTerritoryPoiMap();

    @Accessor()
    Map<String, TerritoryProfile> getTerritoryProfileMap();

    @Accessor()
    void setTerritoryProfileMap(Map<String, TerritoryProfile> map);

    @Accessor
    Set<TerritoryPoi> getAllTerritoryPois();

    @Accessor()
    void setAllTerritoryPois(Set<TerritoryPoi> pois);

    @Invoker
    @Nullable
    TerritoryPoi callGetTerritoryPoiFromAdvancement(String name);
}
