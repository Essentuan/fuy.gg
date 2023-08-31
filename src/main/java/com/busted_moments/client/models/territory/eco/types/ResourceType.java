package com.busted_moments.client.models.territory.eco.types;

import net.minecraft.ChatFormatting;

public enum ResourceType {
   EMERALDS(ChatFormatting.GREEN, "Emeralds", ""),
   ORE(ChatFormatting.WHITE, "Ore", "Ⓑ"),
   WOOD(ChatFormatting.GOLD, "Wood", "Ⓒ"),
   FISH(ChatFormatting.AQUA, "Fish", "Ⓚ"),
   CROP(ChatFormatting.YELLOW, "Crops", "Ⓙ");

   private final ChatFormatting color;
   private final String name;
   private final String symbol;

   ResourceType(ChatFormatting color, String name, String symbol) {
      this.color = color;
      this.name = name;
      this.symbol = symbol;
   }

   public ChatFormatting getColor() {
      return this.color;
   }

   public String getSymbol() {
      return this.symbol;
   }

   public String getName() {
      return this.name;
   }

   public String getPrettySymbol() {
      return this.color + this.symbol + (!this.symbol.isEmpty() ? " " : "");
   }
}
