package com.busted_moments.mixin;

import com.busted_moments.buster.api.Territory;
import com.busted_moments.buster.types.guilds.TerritoryProfile;
import com.busted_moments.client.buster.TerritoryList;
import com.busted_moments.client.framework.wynntils.WynntilsKt;
import com.busted_moments.client.framework.events.EventsKt;
import com.busted_moments.client.models.territories.eco.EcoConstants;
import com.llamalad7.mixinextras.sugar.Local;
import com.wynntils.mc.event.AdvancementUpdateEvent;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.TerritoryModel;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.services.map.pois.TerritoryPoi;
import com.wynntils.utils.type.CappedValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(value = TerritoryModel.class, remap = false)
public abstract class TerritoryModelMixin {
    @Mutable
    @Shadow
    private Set<TerritoryPoi> allTerritoryPois;

    @Inject(
            method = "onAdvancementUpdate",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Set;iterator()Ljava/util/Iterator;",
                    shift = At.Shift.BEFORE
            )
    )
    public void onAdvancementUpdate(
            AdvancementUpdateEvent event, CallbackInfo ci, @Local Map<String, TerritoryInfo> tempMap
    ) {
        if (TerritoryList.ProfileUpdateEvent.Companion.isReady()) {
            var profiles = new HashMap<String, TerritoryProfile>();

            for (var entry : tempMap.entrySet()) {
                var name = entry.getKey();

                if (!TerritoryList.INSTANCE.contains(name))
                    continue;

                var info = entry.getValue();

                var resources = new EnumMap<Territory.Resource, TerritoryProfile.Resources>(Territory.Resource.class);

                for (var resource : GuildResource.values()) {
                    var stored = info.getStorage(resource);

                    if (stored == null) {
                        if (resource == GuildResource.EMERALDS) {
                            if (info.isHeadquarters())
                                stored = new CappedValue(0, EcoConstants.HQ_EMERALD_STORAGE);
                            else
                                stored = new CappedValue(0, EcoConstants.NORMAL_EMERALD_STORAGE);
                        } else if (info.isHeadquarters()) {
                            stored = new CappedValue(0, EcoConstants.HQ_RESOURCE_STORAGE);
                        } else {
                            stored = new CappedValue(0, EcoConstants.NORMAL_RESOURCE_STORAGE);
                        }
                    }

                    resources.put(
                        WynntilsKt.getBuster(resource),
                        new TerritoryProfile.Resources(
                            info.getGeneration(resource),
                            stored.current(),
                            stored.max()
                        )
                    );
                }

                profiles.put(
                    name,
                    new TerritoryProfile(
                        name,
                        info.getGuildName(),
                        resources,
                        new HashSet<>(info.getTradingRoutes()),
                        WynntilsKt.getBuster(info.getDefences()),
                        info.isHeadquarters()
                    )
                );
            }

            EventsKt.post(new TerritoryList.ProfileUpdateEvent(profiles));
        }
    }

    @Inject(method = "updateTerritoryProfileMap", at = @At("HEAD"), cancellable = true)
    public void updateTerritoryProfileMap(CallbackInfo ci) {
        if (!TerritoryList.INSTANCE.isEmpty())
            ci.cancel();
    }
}
