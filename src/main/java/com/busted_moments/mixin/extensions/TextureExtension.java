package com.busted_moments.mixin.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.services.map.pois.TerritoryPoi;
import com.wynntils.utils.render.Texture;
import net.essentuan.esl.color.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
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

   @Override
   public void render(@NotNull PoseStack poseStack, float x, float y, float z, float width, float height, int ux, int uy, Color color) {
      com.busted_moments.client.framework.render.Texture.DefaultImpls.render(
              this,
              poseStack,
              x,
              y,
              z,
              width,
              height,
              ux,
              uy,
              color
      );
   }

    @Override
   public void render(@NotNull PoseStack poseStack, @NotNull MultiBufferSource.BufferSource bufferSource, float x, float y, float z, float width, float height, int ux, int uy, Color color) {
      com.busted_moments.client.framework.render.Texture.DefaultImpls.render(
              this,
              poseStack,
              bufferSource,
              x,
              y,
              z,
              width,
              height,
              ux,
              uy,
              color
      );
   }
}
