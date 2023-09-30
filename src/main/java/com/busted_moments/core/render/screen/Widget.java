package com.busted_moments.core.render.screen;

import com.busted_moments.core.render.TextureInfo;
import com.busted_moments.core.render.screen.elements.TextBoxElement;
import com.busted_moments.core.render.screen.widgets.ItemStackWidget;
import com.busted_moments.core.render.screen.elements.RectElement;
import com.busted_moments.core.render.screen.elements.TextureElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class Widget<This extends Widget<This>> implements Screen.Widget<This> {
   private float scale = 1;

   protected float x = 0;
   protected float y = 0;
   
   private float width = 1;
   private float height = 1;
   
   private float originalWidth;
   private float originalHeight;

   protected boolean active = true;
   protected boolean visible = true;

   private boolean isHovered = false;

   private boolean isFocused = false;

   private HoverEvent.Handler<This> HOVER_PRE = (mouseX, mouseY, widget) -> {};
   private HoverEvent.Handler<This> HOVER_POST = (mouseX, mouseY, widget) -> {};

   private ClickEvent.Handler<This> CLICK = (mouseX, mouseY, button, widget) -> false;


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

   public void setWidth(float width) {
      originalWidth = width;
      this.width = width * scale;
   }

   public float getWidth() {
      return width;
   }

   public void setHeight(float height) {
      originalHeight = height;
      this.height = height * scale;
   }

   public float getHeight() {
      return height;
   }

   public This setSize(float width, float height) {
      setWidth(width);
      setHeight(height);

      return getThis();
   }

   public float getScale() {
      return scale;
   }

   public This setScale(float scale) {
      this.scale = scale;

      setWidth(originalWidth);
      setHeight(originalHeight);

      return getThis();
   }

   public This onHover(HoverEvent.Handler<This> handler, HoverEvent type) {
      if (type == HoverEvent.PRE) HOVER_PRE = handler;
      else HOVER_POST = handler;

      return getThis();
   }

   protected boolean onMouseDown(double mouseX, double mouseY, int button) {
      return false;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return onMouseDown(mouseX, mouseY, button) || (isMouseOver(mouseX, mouseY) && CLICK.accept(mouseX, mouseY, button, getThis()));
   }

   public This onClick(ClickEvent.Handler<This> handler) {
      CLICK = handler;

      return getThis();
   }

   private void setHovered(int mouseX, int mouseY) {
      this.isHovered = isMouseOver(mouseX, mouseY);
   }

   @Override
   public boolean isMouseOver(double mouseX, double mouseY) {
      return mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + getWidth() && mouseY < this.getY() + getHeight();
   }

   protected abstract void onRender(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick);

   @Override
   public void render(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      if (!isActive()) return;

      PoseStack poseStack = graphics.pose();

      setHovered(mouseX, mouseY);

      if (isHovered()) HOVER_PRE.accept(mouseX, mouseY, getThis());

      if (scale == 1F) onRender(graphics, bufferSource, mouseX, mouseY, partialTick);
      else {
         this.width = originalWidth;
         this.height = originalHeight;

         poseStack.pushPose();
         poseStack.scale(scale, scale, 1);

         float originalX = getX();
         float originalY = getY();

         setX(originalX / scale);
         setY(originalY / scale);

         onRender(graphics, bufferSource, mouseX, mouseY, partialTick);

         poseStack.popPose();

         setX(originalX);
         setY(originalY);

         setWidth(originalWidth);
         setHeight(originalHeight);
      }

      if (isHovered()) HOVER_POST.accept(mouseX, mouseY, getThis());
   }

   @Override
   public void setFocused(boolean focused) {
      this.isFocused = focused;
   }

   @Override
   public boolean isFocused() {
      return isFocused;
   }

   public This setActive(boolean active) {
      this.active = active;

      return getThis();
   }

   public boolean isActive() {
      return active;
   }

   public boolean isHovered() {
      return isHovered;
   }

   protected class TextBox extends TextBoxElement<TextBox> {

      public TextBox(StyledText text, float x, float y) {
         super(text, x, y);
      }

      public TextBox(Component text, float x, float y) {
         super(text, x, y);
      }

      public TextBox(String string, float x, float y) {
         super(string, x, y);
      }

      @Override
      public Screen.Element getElement() {
         return Widget.this.getElement();
      }
   }

   protected class Texture extends TextureElement<Texture> {
      public Texture() {}

      public Texture(TextureInfo texture) {
         setTexture(texture);
      }

      @Override
      public Screen.Element getElement() {
         return Widget.this.getElement();
      }
   }

   protected class Rect extends RectElement<Rect> {

      @Override
      public Screen.Element getElement() {
         return Widget.this.getElement();
      }
   }

   protected class Item extends ItemStackWidget<Item> {
      public Item(ItemStack item) {
         setItem(item);
      }

      @Override
      public Screen.Element getElement() {
         return Widget.this.getElement();
      }
   }

   protected class ItemTooltip extends ItemStackWidget.Tooltip<ItemTooltip> {
      public ItemTooltip() {}

      public ItemTooltip(ItemStack item) {
         setItem(item);
      }

      @Override
      public Screen.Element getElement() {
         return Widget.this.getElement();
      }
   }
}
