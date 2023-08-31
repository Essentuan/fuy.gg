package com.busted_moments.client.models.territory.eco;

import com.busted_moments.client.models.territory.eco.types.UpgradeType;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynntils.utils.mc.McUtils.player;

public record Upgrade(UpgradeType type, int level) {
   public long getCost() {
      return type.getLevel(level).cost();
   }

   public double getBonus() {
      return type.getLevel(level).bonus();
   }
}
