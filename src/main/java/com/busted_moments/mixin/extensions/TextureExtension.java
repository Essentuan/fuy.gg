package com.busted_moments.mixin.extensions;

import com.wynntils.utils.render.Texture;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Texture.class, remap = false)
public abstract class TextureExtension implements com.busted_moments.client.framework.render.Texture {
   @Shadow public abstract ResourceLocation resource();

   @Shadow public abstract int width();

   @Shadow public abstract int height();

   @NotNull
   @Override
   public ResourceLocation getResource() {
      return this.resource();
   }

   @Override
   public float getWidth() {
      return this.width();
   }

   @Override
   public float getHeight() {
      return this.height();
   }
}
