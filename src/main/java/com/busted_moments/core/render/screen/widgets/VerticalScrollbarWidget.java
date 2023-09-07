package com.busted_moments.core.render.screen.widgets;

import com.busted_moments.core.render.TextureInfo;
import com.busted_moments.core.render.screen.Widget;
import com.busted_moments.core.tuples.Pair;
import me.shedaniel.clothconfig2.impl.EasingMethod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class VerticalScrollbarWidget<This extends VerticalScrollbarWidget<This>> extends Widget<This> {
   private double slider = 0.0;
   private Pair<Float, Double> dragging = null;

   private double intensity = 1D;

   private EasingMethod easing = EasingMethod.EasingMethodImpl.LINEAR;
   private ScrollProgress progress = null;

   private Consumer<Double> ON_SCROLL = e -> {
   };

   public double getSlider() {
      return slider;
   }

   private void setSlider(double value) {
      slider = Mth.clamp(value, 0, 1);
      ON_SCROLL.accept(slider);
   }

   public boolean isDragging() {
      return dragging != null;
   }

   private float getSliderY() {
      return (float) (getY() + (slider * (getHeight() - texture.height())));
   }

   private TextureInfo texture;

   public TextureInfo getTexture() {
      return texture;
   }

   public This setTexture(TextureInfo texture) {
      this.texture = texture;

      return getThis();
   }

   public This onScroll(@NotNull Consumer<Double> consumer) {
      ON_SCROLL = consumer;

      return getThis();
   }

   public double getScroll() {
      return slider;
   }

   public EasingMethod getEasing() {
      return easing;
   }

   public This setEasing(EasingMethod easing) {
      this.easing = easing;

      return getThis();
   }

   public This setScrollIntensity(double intensity) {
      this.intensity = intensity;

      return getThis();
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      if (this.active && this.visible && button == 0 && dragging != null) {
         var sliderY = dragging.one() + mouseY - dragging.two();
         setSlider((sliderY - getY()) / (getHeight() - texture.height()));
         progress = null;

         return true;
      }
      return false;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      if (button == 0 && dragging != null) {
         dragging = null;
         return true;
      }
      return false;
   }

   @Override
   public boolean onMouseDown(double mouseX, double mouseY, int button) {
      if (this.active && this.visible && button == 0 && dragging == null && mouseX >= getX() && mouseX <= getX() + getWidth()) {
         var sliderY = getSliderY();
         if (mouseY >= sliderY && mouseY <= sliderY + texture.height()) {
            dragging = Pair.of(sliderY, mouseY);
            return true;
         } else if (mouseY >= getY() && mouseY <= getY() + getHeight()) {
            setSlider((mouseY - texture.height() / 2F - getY()) / (getHeight() - texture.height()));
            progress = null;
            dragging = Pair.of(sliderY, mouseY);

            return true;
         }
      }
      return false;
   }

   @Override
   public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      var sliderY = getSliderY();
      if (mouseY >= sliderY && mouseY <= sliderY + texture.height()) return scroll(delta);

      return false;
   }

   public boolean scroll(double delta) {
      double amount = ((delta / getHeight()) * -1 * intensity);

      progress = new ScrollProgress(amount, 200);

      dragging = null;

      return true;
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      float sliderY;

      return (mouseX >= getX() && mouseX <= getX() + getWidth()) && (mouseY >= (sliderY = getSliderY()) && mouseY <= sliderY + texture.height());
   }

   @Override
   protected void onRender(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      new Texture(texture).setPosition(getX(), getSliderY()).build();

      if (progress != null) progress.update();
   }

   private class ScrollProgress {
      private final double amount;
      private final double duration;
      private final double time;

      private final double start;

      private ScrollProgress(double amount, long duration) {
         this.start = slider;

         this.amount = amount;
         this.duration = duration;
         this.time = System.currentTimeMillis();
      }

      void update() {
         if (System.currentTimeMillis() > time + duration) progress = null;
         else setSlider(start + getEasing().apply(progress()) * amount);
      }

      double progress() {
         return (System.currentTimeMillis() - time) / duration;
      }
   }
}
