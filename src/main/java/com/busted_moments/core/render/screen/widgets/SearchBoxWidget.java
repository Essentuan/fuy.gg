package com.busted_moments.core.render.screen.widgets;

import com.busted_moments.core.render.FillStyle;
import com.busted_moments.core.render.screen.Screen;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.screens.base.TextboxScreen;
import com.wynntils.screens.base.widgets.SearchWidget;
import me.shedaniel.math.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

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

   public void setWidth(int width) {
      originalWidth = width;
      this.width = (int) (width * scale);
   }

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

   @Override
   public void render(@NotNull PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      this.width = originalWidth;
      this.height = originalHeight;

      poseStack.pushPose();
      poseStack.scale(scale, scale, 1);

      int originalX = getX();
      int originalY = getY();

      setX((int) (originalX / scale));
      setY((int) (originalY / scale));

      bufferSource.endLastBatch();
      render(poseStack, mouseX, mouseY, partialTick);

      poseStack.popPose();

      setX(originalX);
      setY(originalY);

      setWidth(originalWidth);
      setHeight(originalHeight);
   }
}
