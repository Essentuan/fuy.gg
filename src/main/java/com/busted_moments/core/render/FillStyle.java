package com.busted_moments.core.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Color;

public interface FillStyle {
   void render(PoseStack poseStack, float x, float y, float width, float height);

   record Solid(Color color) implements FillStyle {
      @Override
      public void render(PoseStack poseStack, float x, float y, float width, float height) {
         Renderer.fill(poseStack, x, y, width, height, color);
      }
   }

   record Gradient(Color from, Color to) implements FillStyle {
      @Override
      public void render(PoseStack poseStack, float x, float y, float width, float height) {
         Renderer.gradient(poseStack, x, y, width, height, from, to);
      }
   }
}
