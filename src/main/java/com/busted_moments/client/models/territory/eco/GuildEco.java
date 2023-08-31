package com.busted_moments.client.models.territory.eco;

import com.busted_moments.client.models.territory.TerritoryModel;
import com.busted_moments.core.api.requests.mapstate.Territory;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GuildEco implements Territory.List<TerritoryEco> {
   private final Map<String, TerritoryEco> territories = new LinkedHashMap<>();
   private final Date timestamp = new Date();

   TerritoryEco HQ = null;
   public GuildEco(List<ItemStack> items) {
      for (ItemStack item : items) {
         TerritoryEco territory = new TerritoryEco(this, item);
         territories.put(territory.getName(), territory);
      }

      var state = TerritoryModel.getTerritoryList();

      state.getVersion().thenAccept(optional -> optional.ifPresent(version -> version.getTemplate().thenAccept(template -> {
         Route.visit(state, template, this, false);
         Route.visit(state, template, this, true);
      })));
   }

   @Override
   public TerritoryEco get(String territory) {
      return territories.get(territory);
   }

   @Override
   public boolean contains(String territory) {
      return territories.containsKey(territory);
   }

   @Override
   public Date getTimestamp() {
      return timestamp;
   }

   @Override
   public int size() {
      return territories.size();
   }

   @Override
   public boolean isEmpty() {
      return territories.isEmpty();
   }

   @NotNull
   @Override
   public Iterator<TerritoryEco> iterator() {
      return territories.values().iterator();
   }

   @NotNull
   @Override
   public Object @NotNull [] toArray() {
      return territories.values().toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return territories.values().toArray(a);
   }
}
