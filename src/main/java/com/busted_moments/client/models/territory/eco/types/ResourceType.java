package com.busted_moments.client.models.territory.eco.types;

import com.busted_moments.core.render.TextureInfo;
import net.minecraft.ChatFormatting;

import static com.busted_moments.client.util.Textures.TerritoryMenu.Production;

public enum ResourceType {
   EMERALDS(Production.EMERALD, ChatFormatting.GREEN, "Emeralds", ""),
   ORE(Production.ORE, ChatFormatting.WHITE, "Ore", "Ⓑ"),
   WOOD(Production.WOOD, ChatFormatting.GOLD, "Wood", "Ⓒ"),
   FISH(Production.FISH, ChatFormatting.AQUA, "Fish", "Ⓚ"),
   CROP(Production.CROP, ChatFormatting.YELLOW, "Crops", "Ⓙ");

   private final TextureInfo texture;

   private final ChatFormatting color;
   private final String name;
   private final String symbol;

   ResourceType(TextureInfo texture, ChatFormatting color, String name, String symbol) {
      this.texture = texture;
      this.color = color;
      this.name = name;
      this.symbol = symbol;
   }

   public TextureInfo getTexture() {
      return texture;
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

   public static ResourceType of(String string) {
      for (ResourceType resource : values()) {
         if (resource.toString().toLowerCase().startsWith(string.toLowerCase()) || string.toLowerCase().startsWith(resource.toString().toLowerCase()))
            return resource;
      }

      throw new IllegalArgumentException("Cannot find resource with name %s".formatted(string));
   }
}
