package com.busted_moments.core.render.screen.elements;

import com.busted_moments.core.render.FillStyle;
import com.busted_moments.core.render.screen.ScreenElement;
import me.shedaniel.math.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

public abstract class RectElement<This extends RectElement<This>> extends ScreenElement.Sizable<This> {
   private FillStyle style;

   private float width = 0;
   private float height = 0;

   @Override
   public This setWidth(float width) {
      this.width = width;
      return getThis();
   }

   @Override
   public float getWidth() {
      return width;
   }

   @Override
   public This setHeight(float height) {
      this.height = height;
      return getThis();
   }

   @Override
   public float getHeight() {
      return height;
   }

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
   public void render(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      style.render(
              graphics.pose(),
              getX(),
              getY(),
              getWidth(),
              getHeight()
      );
   }
}
