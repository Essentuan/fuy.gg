package com.busted_moments.core.render;

import com.wynntils.utils.render.Texture;
import net.minecraft.resources.ResourceLocation;

public record TextureInfo(ResourceLocation location, int width, int height) {
   public TextureInfo(String key, String location, int width, int height) {
      this(new ResourceLocation(key, location), width, height);
   }

   public TextureInfo(Texture texture) {
      this(texture.resource(), texture.width(), texture.height());
   }
}
