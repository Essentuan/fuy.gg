package com.busted_moments.core.render.screen.widgets;

import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.TextureInfo;
import com.busted_moments.core.render.screen.Widget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

public abstract class TexturedButtonWidget<This extends TexturedButtonWidget<This>> extends Widget<This> {
   private TextureInfo texture;

   public TextureInfo getTexture() {
      return texture;
   }

   public This setTexture(TextureInfo texture) {
      this.texture = texture;

      setWidth(texture.width());
      setHeight(texture.height());

      return getThis();
   }

   public This setTexture(com.wynntils.utils.render.Texture texture) {
      return setTexture(new TextureInfo(texture));
   }

   @Override
   protected void onRender(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      Renderer.texture(graphics.pose(), getX(), getY(), getWidth(), getHeight(), texture);
   }
}
