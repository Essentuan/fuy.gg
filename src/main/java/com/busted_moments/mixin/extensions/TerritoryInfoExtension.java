package com.busted_moments.mixin.extensions;

import com.busted_moments.buster.api.Territory;
import com.busted_moments.client.buster.TerritoryList;
import com.busted_moments.client.framework.artemis.ArtemisKt;
import com.busted_moments.client.framework.artemis.TerritoryCopier;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mixin(value = TerritoryInfo.class, remap = false)
public abstract class TerritoryInfoExtension implements TerritoryCopier {
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

   @Override
   public void copyOf(Territory territory) {
      guildName = territory.getOwner().getName();
      guildPrefix = territory.getOwner().getTag();

      storage = new HashMap<>();
      generators = new HashMap<>();

      for (var entry : territory.getResources().entrySet()) {
         var resource = ArtemisKt.getArtemis(entry.getKey());
         var storage = entry.getValue();

         if (storage.getStored() != 0)
            this.storage.put(
                    resource,
                    new CappedValue(
                            storage.getStored(),
                            capacity(territory, storage, resource)
                    )
            );

         var prod = Math.max(storage.getProduction(), storage.getBase());
         if (prod != 0)
            this.generators.put(resource, prod);
      }

      tradingRoutes = new ArrayList<>(territory.getConnections());

      defences = ArtemisKt.getArtemis(territory.getDefense());
      treasury = ArtemisKt.getArtemis(territory.getTreasury());

      headquarters = territory.getHq();

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
