package com.busted_moments.core.render.screen.widgets;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.render.screen.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import com.wynntils.screens.base.widgets.TextInputBoxWidget;
import com.wynntils.utils.render.FontRenderer;
import com.wynntils.utils.type.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public abstract class SearchBoxWidget<This extends SearchBoxWidget<This>> extends SearchWidget implements Screen.Widget<This> {
   private float scale = 1;

   private int originalWidth;
   private int originalHeight;

   public SearchBoxWidget(int x, int y, int width, int height, Consumer<String> onUpdateConsumer, TextboxScreen textboxScreen) {
      super(x, y, width, height, onUpdateConsumer, textboxScreen);

      this.originalWidth = width;
      this.originalHeight = height;
   }

   @Override
   protected boolean clicked(double mouseX, double mouseY) {
      return super.clicked(mouseX, mouseY);
   }

   @Override
   public void setWidth(int width) {
      originalWidth = width;
      this.width = (int) (width * scale);
   }

   @Override
   public void setHeight(int height) {
      originalHeight = height;
      this.height = (int) (height * scale);
   }

   public This offsetX(int x) {
      setX(getX() - x);

      return getThis();
   }

   public This offsetY(int y) {
      setY(getY() - y);

      return getThis();
   }

   public This offset(int x, int y) {
      return offsetX(x).offsetY(y);
   }

   public This setScale(float scale) {
      this.scale = scale;

      setWidth(originalWidth);
      setHeight(originalHeight);

      return getThis();
   }

   private static Method RENDERED_TEXT_METHOD;

   @SuppressWarnings("unchecked")
   private Pair<String, Integer> getRenderedText(float maxTextWidth) {
      if (RENDERED_TEXT_METHOD == null) {
         try {
            RENDERED_TEXT_METHOD = TextInputBoxWidget.class.getDeclaredMethod("getRenderedText", float.class);
            RENDERED_TEXT_METHOD.setAccessible(true);
         } catch (NoSuchMethodException e) {
            throw UnexpectedException.propagate(e);
         }
      }

      try {
         return (Pair<String, Integer>) RENDERED_TEXT_METHOD.invoke(this, maxTextWidth);
      } catch (IllegalAccessException | InvocationTargetException e) {
         throw UnexpectedException.propagate(e);
      }
   }

   @Override
   protected int getIndexAtPosition(double mouseX) {
      mouseX -= this.getX();
      mouseX -= textPadding;

      Pair<String, Integer> renderedTextDetails = getRenderedText(getMaxTextWidth());
      String renderedText = renderedTextDetails.a();
      int shift = renderedTextDetails.b();

      Font font = FontRenderer.getInstance().getFont();

      if (font.width(renderedText) < mouseX) {
         return renderedText.length() + shift;
      }

      int closestWidthCharIndex = 0;
      double closestDistance = Double.MAX_VALUE;
      for (int i = 0; i < renderedText.length(); i++) {
         float width = font.width(renderedText.substring(0, i)) * scale;
         double distance = Math.abs(mouseX - width);

         if (distance > closestDistance) break;

         closestDistance = distance;
         closestWidthCharIndex = i;
      }

      return closestWidthCharIndex + shift;
   }

   @Override
   public void render(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      this.width = originalWidth;
      this.height = originalHeight;

      PoseStack poseStack = graphics.pose();

      poseStack.pushPose();
      poseStack.scale(scale, scale, 1);

      int originalX = getX();
      int originalY = getY();

      setX((int) (originalX / scale));
      setY((int) (originalY / scale));

      bufferSource.endLastBatch();
      render(graphics, mouseX, mouseY, partialTick);

      poseStack.popPose();

      setX(originalX);
      setY(originalY);

      setWidth(originalWidth);
      setHeight(originalHeight);
   }
}
