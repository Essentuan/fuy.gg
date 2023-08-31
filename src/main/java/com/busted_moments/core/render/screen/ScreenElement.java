package com.busted_moments.core.render.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class ScreenElement<This extends ScreenElement<This>> implements Screen.Object<This, ScreenElement<?>> {
   protected float x = 0;
   protected float y = 0;

   public abstract void render(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick);

   public float getX() {
      return x;
   }

   public This setX(float x) {
      this.x = x;

      return getThis();
   }

   public float getY() {
      return y;
   }

   public This setY(float y) {
      this.y = y;

      return getThis();
   }

   public This setPosition(float x, float y) {
      return setX(x).setY(y);
   }

   public This offsetX(float x) {
      this.x-= x;

      return getThis();
   }

   public This offsetY(float y) {
      this.y-= y;

      return getThis();
   }

   public This offset(float x, float y) {
      return offsetX(x).offsetY(y);
   }

   public This center() {
      Screen.Element element = getElement();

      return setX(element.width/2F).setY(element.width/2F);
   }

   public This build() {
      getElement().elements.add(this);

      return getThis();
   }

   public abstract static class Sizable<This extends Sizable<This>> extends ScreenElement<This> {
      public abstract This setWidth(float width);
      public abstract float getWidth();

      public abstract float getHeight();
      public abstract This setHeight(float height);

      public This setSize(float width, float height) {
         return setWidth(width).setHeight(height);
      }

      @Override
      public This center() {
         Screen.Element element = getElement();

         return setX((element.width/2F) - getWidth()/2).setY((element.height/2F) - getHeight()/2);
      }
   }
}
