package com.busted_moments.core.render.overlay.elements;

import com.busted_moments.core.render.FillStyle;
import com.busted_moments.core.render.overlay.Overlays;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.shedaniel.math.Color;
import net.minecraft.client.renderer.MultiBufferSource;

public abstract class RectElement<This extends RectElement<This>> extends Overlays.Element<This> {
   private FillStyle style;

   public This setStyle(FillStyle style) {
      this.style = style;

      return getThis();
   }

   public This setFill(int r, int g, int b, int a) {
      return setFill(Color.ofRGBA(r, g, b, a));
   }

   public This setFill(int rgba) {
      return setFill(Color.ofTransparent(rgba));
   }


   public This setFill(Color color) {
      return setStyle(new FillStyle.Solid(color));
   }

   public This setGradient(Color from, Color to) {
      return setStyle(new FillStyle.Gradient(from, to));
   }


   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
      style.render(poseStack,
              getX(),
              getY(),
              getWidth(),
              getHeight()
      );
   }
}
