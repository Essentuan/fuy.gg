package com.busted_moments.core.render;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import static com.wynntils.utils.mc.McUtils.mc;
import static com.wynntils.utils.render.RenderUtils.drawTexturedRect;

public interface Renderer {
   static void text(
           PoseStack poseStack,
           MultiBufferSource bufferSource,
           StyledText text,
           float x,
           float y,
           float maxWidth,
           CustomColor customColor,
           TextShadow textShadow,
           float textScale
   ) {
      text(poseStack, bufferSource, text, x, x + 100, y, y + 100, maxWidth, customColor, HorizontalAlignment.LEFT, VerticalAlignment.TOP, textShadow, textScale);
   }
   
   static void text(
           PoseStack poseStack,
           MultiBufferSource bufferSource,
           StyledText text,
           float x1,
           float x2,
           float y1,
           float y2,
           float maxWidth,
           CustomColor customColor,
           HorizontalAlignment horizontalAlignment,
           VerticalAlignment verticalAlignment,
           TextShadow textShadow,
           float textScale
   ) {
      FontRenderer.draw(poseStack, bufferSource, text, x1, x2, y1, y2, maxWidth, customColor, horizontalAlignment, verticalAlignment, textShadow, textScale);
   }
   
   static void texture(PoseStack poseStack, float x, float y, TextureInfo texture) {
      texture(
              poseStack,
              x, y,
              (float) texture.width(),
              (float) texture.height(),
              texture
      );
   }

   static void texture(PoseStack poseStack, float x, float y, float width, float height, TextureInfo texture) {
      drawTexturedRect(
              poseStack,
              texture.location(),
              x, y,
              width,
              height,
              texture.width(),
              texture.height()
      );
   }

   static void fill(PoseStack poseStack, float x, float y, float width, float height, Color color) {
      fill(poseStack, x, y, width, height, 0, color);
   }

   static void fill(PoseStack poseStack, float x, float y, float width, float height, CustomColor color) {
      fill(poseStack, x, y, width, height, 0, Color.ofTransparent(color.asInt()));
   }

   static void fill(PoseStack poseStack, float x, float y, float width, float height, float z, Color color) {
      float endX = x + width;
      float endY = y + height;

      Matrix4f matrix4f = poseStack.last().pose();
      float i;
      if (x < endX) {
         i = x;
         x = endX;
         endX = i;
      }

      if (y < endY) {
         i = y;
         y = endY;
         endY = i;
      }

      float alpha = color.getAlpha() / 255.0F;
      float red = color.getRed() / 255.0F;
      float green = color.getGreen() / 255.0F;
      float blue = color.getBlue() / 255.0F;
      BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
      bufferBuilder.vertex(matrix4f, x, y, z).color(red, green, blue, alpha).endVertex();
      bufferBuilder.vertex(matrix4f, x, endY, z).color(red, green, blue, alpha).endVertex();
      bufferBuilder.vertex(matrix4f, endX, endY, z).color(red, green, blue, alpha).endVertex();
      bufferBuilder.vertex(matrix4f, endX, y, z).color(red, green, blue, alpha).endVertex();
      BufferUploader.drawWithShader(bufferBuilder.end());
      RenderSystem.disableBlend();
   }

   static void gradient(PoseStack poseStack, float x, float y, float width, float height, Color colorFrom, Color colorTo) {
      gradient(poseStack, x, y, width, height, colorFrom, colorTo, 0);
   }

   static void gradient(PoseStack poseStack, float x, float y, float width, float height, Color colorA, Color colorB, float blitOffset) {
      RenderSystem.enableBlend();
      RenderSystem.setShader(GameRenderer::getPositionColorShader);
      Tesselator tesselator = Tesselator.getInstance();
      BufferBuilder builder = tesselator.getBuilder();
      builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

      Matrix4f matrix = poseStack.last().pose();

      float x2 = x + width;
      float y2 = y + height;

      float alphaA = colorA.getAlpha() / 255.0F;
      float redA = colorA.getRed() / 255.0F;
      float greenA = colorA.getGreen() / 255.0F;
      float blueA = colorA.getBlue() / 255.0F;
      float alphaB = colorB.getAlpha() / 255.0F;
      float redB = colorB.getRed() / 255.0F;
      float greenB = colorB.getGreen() / 255.0F;
      float blueB = colorB.getBlue() / 255.0F;

      builder.vertex(matrix, x, y, blitOffset).color(redA, greenA, blueA, alphaA).endVertex();
      builder.vertex(matrix, x, y2, blitOffset).color(redB, greenB, blueB, alphaB).endVertex();
      builder.vertex(matrix, x2, y2, blitOffset).color(redB, greenB, blueB, alphaB).endVertex();
      builder.vertex(matrix, x2, y, blitOffset).color(redA, greenA, blueA, alphaA).endVertex();
      tesselator.end();
      RenderSystem.disableBlend();
   }

   static void gui_item(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, ItemStack stack, float x, float y) {
      gui_item(poseStack, bufferSource, stack, x, y, mc().getItemRenderer().getModel(stack, null, null, 0));
   }

   static void gui_item(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, ItemStack stack, float x, float y, BakedModel bakedModel) {
      poseStack.pushPose();
      poseStack.translate(x, y, 100.0F);
      poseStack.translate(8.0F, 8.0F, 0.0F);
      poseStack.mulPoseMatrix(new Matrix4f().scaling(1.0F, -1.0F, 1.0F));
      poseStack.scale(16.0F, 16.0F, 16.0F);
      boolean bl = !bakedModel.usesBlockLight();
      if (bl) {
         Lighting.setupForFlatItems();
      }

      PoseStack poseStack2 = RenderSystem.getModelViewStack();
      poseStack2.pushPose();
      poseStack2.mulPoseMatrix(poseStack.last().pose());
      RenderSystem.applyModelViewMatrix();
      mc().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, new PoseStack(), bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
      bufferSource.endBatch();
      RenderSystem.enableDepthTest();
      if (bl) {
         Lighting.setupFor3DItems();
      }

      poseStack.popPose();
      poseStack2.popPose();
      RenderSystem.applyModelViewMatrix();
   }

   static void mask(PoseStack poseStack, float x, float y, TextureInfo texture) {
      GL11.glEnable(2960);
      RenderSystem.stencilMask(255);
      RenderSystem.clear(1024, true);
      RenderSystem.stencilFunc(519, 1, 255);
      RenderSystem.stencilOp(7680, 7680, 7681);
      RenderSystem.colorMask(false, false, false, false);
      RenderSystem.depthMask(false);
      texture(poseStack, x, y, texture);
      RenderSystem.colorMask(true, true, true, true);
      RenderSystem.depthMask(true);
      RenderSystem.stencilMask(0);
      RenderSystem.stencilOp(7680, 7680, 7680);
      RenderSystem.stencilFunc(514, 1, 255);
   }

   static void clear_mask() {
      RenderSystem.clear(1024, true);
      RenderSystem.stencilOp(7680, 7680, 7680);
      RenderSystem.stencilMask(0);
      RenderSystem.stencilFunc(519, 0, 255);
   }

   static void beacon(Beacon.Provider provider) {
      BeaconRenderer.register(provider);
   }
}
