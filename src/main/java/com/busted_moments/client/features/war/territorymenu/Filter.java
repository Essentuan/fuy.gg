package com.busted_moments.client.features.war.territorymenu;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.models.territory.eco.types.UpgradeType;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.collector.LinkedSetCollector;
import com.google.common.collect.Multimap;
import com.wynntils.utils.colors.CustomColor;
import net.minecraft.ChatFormatting;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public enum Filter {
   CUT_OFF_TERRITORY("No Route", territory -> territory.getRoute().isEmpty(), ChatFormatting.RED, true),
   MULTIPLE_BONUSES("Multiple Bonuses", Filter::hasMultipleBonuses, ChatFormatting.WHITE, true),
   MULTI_ATTACK(UpgradeType.MULTI_ATTACK, ChatFormatting.AQUA),
   EMERALD_SEEKING(UpgradeType.EMERALD_SEEKING, ChatFormatting.GREEN),
   TOME_SEEKING(UpgradeType.TOME_SEEKING, ChatFormatting.BLUE),
   MOB_XP(UpgradeType.MOB_EXPERIENCE, ChatFormatting.YELLOW),
   MOB_DAMAGE(UpgradeType.MOB_DAMAGE, ChatFormatting.LIGHT_PURPLE),
   GATHERING_XP(UpgradeType.GATHERING_EXPERIENCE, ChatFormatting.DARK_PURPLE),
   PLACEHOLDER(" ", t -> false, ChatFormatting.WHITE, false),
   STRICT_MODE("Strict Mode", t -> false, ChatFormatting.WHITE, false);

   private final String name;
   private final Predicate<TerritoryEco> predicate;
   private final ChatFormatting textColor;
   private final CustomColor color;

   private final boolean isClickable;

   Filter(String name, Predicate<TerritoryEco> predicate, ChatFormatting textColor, CustomColor color, boolean isClickable) {
      this.name = name;
      this.predicate = predicate;
      this.textColor = textColor;
      this.color = color;
      this.isClickable = isClickable;
   }


   Filter(String name, Predicate<TerritoryEco> predicate, ChatFormatting color, boolean isClickable) {
      this(name, predicate, color, CustomColor.fromChatFormatting(color).withAlpha(60), isClickable);
   }

   Filter(UpgradeType upgrade, ChatFormatting color) {
      this(upgrade.getName(), territory -> territory.hasUpgrade(upgrade), color, true);
   }

   public String getName() {
      return name;
   }

   public String toText(@Nullable Multimap<Filter, ?> counts, Set<Filter> selected, Filter hovered) {
      String base = getName();
      if (isClickable()) base = "[" + (counts == null ? 0 : counts.get(this).size()) + "] " + base;

      if (this.equals(hovered)) base = ChatFormatting.BOLD + base;

      if (!selected.contains(this)) base = ChatUtil.with(ChatFormatting.GRAY, ChatFormatting.STRIKETHROUGH) + base;
      else base = textColor + base;

      return base;
   }

   public CustomColor getColor() {
      return color;
   }

   public boolean isClickable() {
      return isClickable;
   }

   public boolean test(TerritoryEco eco) {
      return predicate.test(eco);
   }

   public static Set<Filter> getFilters(TerritoryEco eco) {
      return Stream.of(values())
              .filter(filter -> filter.test(eco))
              .collect(new LinkedSetCollector<>(e -> e));
   }

   private static final Filter[] BONUSES = new Filter[] {
           MULTI_ATTACK,
           EMERALD_SEEKING,
           TOME_SEEKING,
           MOB_XP,
           MOB_DAMAGE,
           GATHERING_XP,
   };

   private static boolean hasMultipleBonuses(TerritoryEco eco) {
      boolean matched = false;

      for (Filter bonus : BONUSES) {
         boolean matches = bonus.test(eco);
         if (matched && matches) return true;
         matched |= matches;
      }

      return false;
   }
}
