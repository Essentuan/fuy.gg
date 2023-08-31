package com.busted_moments.core.render;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;

import static com.wynntils.utils.render.RenderUtils.drawTexturedRect;

@SuppressWarnings("unchecked")
public interface RenderableElement<This extends RenderableElement<This>> {

   default This getThis() {
      return (This) this;
   }

   void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window);

   float getX();

   This setX(float x);

   float getY();

   This setY(float y);

   default This setPosition(float x, float y) {
      return setX(x).setY(y);
   }

   float getHeight();

   This setHeight(float height);

   float getWidth();

   This setWidth(float width);

   float getScale();

   This setScale(float scale);
}
