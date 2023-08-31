package com.busted_moments.client.models.territory.eco;

import com.busted_moments.client.models.territory.TerritoryModel;
import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.client.models.territory.eco.types.UpgradeType;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.api.requests.mapstate.Territory;
import com.busted_moments.core.tuples.Pair;
import com.wynntils.core.components.Models;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynntils.utils.mc.McUtils.player;

public class TerritoryEco implements Territory {
   private static final Pattern UPGRADE_PATTERN = Pattern.compile("- (?<upgrade>.+) \\[Lv. (?<level>.+)\\]");
   private static final Pattern STORAGE_PATTERN = Pattern.compile("(. )?(?<stored>.+)/(?<capacity>.+) stored");
   private static final Pattern TREASURY_PATTERN = Pattern.compile(". Treasury Bonus: (?<treasury>.+)%");

   private final GuildEco eco;

   private final Territory territory;
   private final ItemStack stack;

   private final boolean isHQ;

   private final Map<ResourceType, Pair<Long, Long>> storage = new HashMap<>();
   private double treasury = 0;
   private final Map<UpgradeType, Upgrade> upgrades = new LinkedHashMap<>();

   Route route = null;
   Route absolute = null;

   protected TerritoryEco(GuildEco eco, ItemStack stack) {
      this.eco = eco;
      this.stack = stack;
      this.isHQ = stack.getDisplayName().getString().contains("(HQ)");
      if (isHQ) eco.HQ = this;

      String territory = getTerritory(stack);

      var list = TerritoryModel.getTerritoryList();

      if (list.contains(territory)) this.territory = list.get(territory);
      else {
         this.territory = new Impl(
                 territory,
                 new Owner.Impl(Models.Guild.getGuildName(), "Unknown"),
                 new Date(),
                 new Location.Impl(0, 0, 0, 0)
         );
      }

      for (Component component : stack.getTooltipLines(player(), TooltipFlag.NORMAL)) {
         String text = ChatUtil.strip(component).replace("Á", "");

         Matcher matcher;

         if (text.startsWith("-") && (matcher = UPGRADE_PATTERN.matcher(text)).matches()) {
            UpgradeType type = UpgradeType.get(matcher.group("upgrade"));
            if (type == null) continue;

            upgrades.put(type, new Upgrade(type, Integer.parseInt(matcher.group("level"))));
         } else if ((matcher = STORAGE_PATTERN.matcher(text)).matches()) {
            long stored = Long.parseLong(matcher.group("stored"));
            long capacity = Long.parseLong(matcher.group("capacity"));

            for (ResourceType resource: ResourceType.values()) {
               if (
                       (resource == ResourceType.EMERALDS && text.charAt(1) == ' ') ||
                       (resource != ResourceType.EMERALDS && text.startsWith(resource.getSymbol()))
               ) storage.put(resource, new Pair<>(stored, capacity));
            }
         } else if (((matcher = TREASURY_PATTERN.matcher(text)).matches())) treasury = Double.parseDouble(matcher.group("treasury"));
      }
   }

   @Override
   public String getName() {
      return territory.getName();
   }

   public ItemStack getItem() {
      return stack;
   }

   public long getStored(ResourceType resourceType) {
      return storage.get(resourceType).one();
   }

   public long getCapacity(ResourceType type) {
      return storage.get(type).two();
   }

   public boolean hasUpgrade(UpgradeType type) {
      return upgrades.containsKey(type);
   }

   public Upgrade getUpgrade(UpgradeType type) {
      return upgrades.getOrDefault(
              type,
              new Upgrade(type, 0)
      );
   }

   public Optional<Route> getRoute() {
      return Optional.ofNullable(route);
   }

   public Optional<Route> getIdealRoute() {
      return Optional.ofNullable(absolute);
   }

   @Override
   public Owner getOwner() {
      return territory.getOwner();
   }

   @Override
   public Date getAcquired() {
      return territory.getAcquired();
   }

   @Override
   public Location getLocation() {
      return territory.getLocation();
   }

   public static String getTerritory(ItemStack stack) {
      String base = ChatUtil.strip(stack.getDisplayName().getString());

      return base.substring(2, base.length() - 1).replace(" (HQ)", "").replace("[!] ", "");
   }

   public static boolean isTerritory(ItemStack stack) {
      return stack.getItem() == Items.PAPER || stack.getItem() == Items.MAP || (stack.getItem() == Items.DIAMOND_AXE && stack.getDamageValue() == 41);
   }

   @Override
   public boolean equals(Object object) {
      if (this == object) return true;
      if (object == null || getClass() != object.getClass()) return false;
      TerritoryEco that = (TerritoryEco) object;
      return Objects.equals(getName(), that.getName());
   }

   @Override
   public int hashCode() {
      return getName().hashCode();
   }
}
