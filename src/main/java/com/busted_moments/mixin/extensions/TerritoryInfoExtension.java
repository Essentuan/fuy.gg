package com.busted_moments.mixin.extensions;

import com.busted_moments.buster.api.Territory;
import com.busted_moments.client.framework.wynntils.WynntilsKt;
import com.busted_moments.client.framework.wynntils.MutableTerritoryPoi;
import com.busted_moments.client.models.territories.eco.EcoConstants;
import com.wynntils.models.territories.TerritoryInfo;
import com.wynntils.models.territories.type.GuildResource;
import com.wynntils.models.territories.type.GuildResourceValues;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.type.CappedValue;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Mixin(value = TerritoryInfo.class, remap = false)
public abstract class TerritoryInfoExtension implements MutableTerritoryPoi {
   @Shadow
   private String guildName;

   @Shadow
   private String guildPrefix;

   @Mutable
   @Shadow
   @Final
   private HashMap<GuildResource, CappedValue> storage;

   @Mutable
   @Shadow
   @Final
   private HashMap<GuildResource, Integer> generators;

   @Mutable
   @Shadow
   @Final
   private List<CustomColor> resourceColors;

   @Mutable
   @Shadow
   @Final
   private boolean headquarters;

   @Mutable
   @Shadow
   @Final
   private List<String> tradingRoutes;

   @Shadow
   private GuildResourceValues defences;

   @Shadow
   private GuildResourceValues treasury;

   @Shadow @Final private static Pattern DEFENSE_PATTERN;

   @Override
   public void setGuildName(@NotNull String s) {
      guildName = s;
   }

   @Override
   public void setGuildPrefix(@NotNull String s) {

   }

   @Override
   public void setStorage(@NotNull Map<@NotNull GuildResource, @NotNull CappedValue> guildResourceCappedValueMap) {
      if (guildResourceCappedValueMap instanceof HashMap<GuildResource, CappedValue> hashmap)
         storage = hashmap;
      else
         storage = new HashMap<>(guildResourceCappedValueMap);
   }

   @Override
   public void setGenerators(@NotNull Map<@NotNull GuildResource, @NotNull Integer> guildResourceIntegerMap) {
      if (guildResourceIntegerMap instanceof HashMap<GuildResource, Integer> hashmap)
         generators = hashmap;
      else
         generators = new HashMap<>(guildResourceIntegerMap);
   }

   @Override
   public void setTradingRoutes(@NotNull List<@NotNull String> strings) {
      tradingRoutes = strings;
   }

   @Override
   public void setTreasury(@NotNull GuildResourceValues guildResourceValues) {
      treasury = guildResourceValues;
   }

   @Override
   public void setDefences(@NotNull GuildResourceValues guildResourceValues) {
      defences = guildResourceValues;
   }

   @Override
   public boolean getHeadquarters() {
      return headquarters;
   }

   @Override
   public void setHeadquarters(boolean b) {
      headquarters = b;
   }

   @Override
   public void generateResourceColors() {
      resourceColors = new ArrayList<>();
      for (var entry : generators.entrySet()) {
         switch (entry.getKey()) {
            case ORE:
               resourceColors.add(CustomColor.fromHSV(0.0F, 0.3F, 1.0F, 1.0F));
               break;
            case FISH:
               resourceColors.add(CustomColor.fromHSV(0.5F, 0.6F, 0.9F, 1.0F));
               break;
            case WOOD:
               resourceColors.add(CustomColor.fromHSV(0.33333334F, 0.6F, 0.9F, 1.0F));
               break;
            case CROPS:
               resourceColors.add(CustomColor.fromHSV(0.16666667F, 0.6F, 0.9F, 1.0F));
               break;
         }
      }
   }

   @Unique
   private static int capacity(Territory territory, Territory.Storage storage, GuildResource resource) {
      final int capacity;

      if (storage.getCapacity() != 0)
         capacity = storage.getCapacity();
      else if (resource == GuildResource.EMERALDS) {
         if (territory.getHq())
            capacity = EcoConstants.HQ_EMERALD_STORAGE;
         else
            capacity = EcoConstants.NORMAL_EMERALD_STORAGE;
      } else {
         if (territory.getHq())
            capacity = EcoConstants.HQ_RESOURCE_STORAGE;
         else
            capacity = EcoConstants.NORMAL_RESOURCE_STORAGE;
      }

      return capacity;
   }
}
