package com.busted_moments.core.render.screen.elements;

import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.TextureInfo;
import com.busted_moments.core.render.screen.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

public abstract class TextureElement<This extends TextureElement<This>> extends ScreenElement.Sizable<This> {
   private TextureInfo texture;

   public TextureInfo getTexture() {
      return texture;
   }

   public This setTexture(TextureInfo texture) {
      this.texture = texture;

      return getThis();
   }

   @Override
   public This setWidth(float width) {
      return getThis();
   }

   @Override
   public float getWidth() {
      return texture.width();
   }

   @Override
   public This setHeight(float height) {
      return getThis();
   }

   @Override
   public float getHeight() {
      return texture.height();
   }

   @Override
   public void render(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      Renderer.texture(graphics.pose(), getX(), getY(), getTexture());
   }
}
