package com.busted_moments.core.render.overlay.elements.textbox;

import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.render.overlay.Overlays;
import com.busted_moments.core.render.overlay.elements.TextElement;
import com.busted_moments.core.render.overlay.elements.RectElement;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public abstract class TextBoxElement<This extends TextBoxElement<This>> extends Overlays.Element<This> {
   private abstract static class RectImpl extends RectElement<RectImpl> {
   }

   private abstract static class TextImpl extends TextElement<TextImpl> {
      protected TextImpl(StyledText text, float x, float y) {
         super(text, x, y);
      }
   }

   private final Padding padding = new Padding(0, 0, 0, 0);
   private final RectElement<?> rect;
   private final TextElement<?> text;

   private boolean dynamic = false;

   @SuppressWarnings("rawtypes")
   protected TextBoxElement(StyledText text, float x, float y) {
      TextBoxElement<?> renderer = this;

      this.rect = new RectImpl() {
         @Override
         protected Hud.Element getElement() {
            return renderer.getElement();
         }
      };
      this.text = new TextImpl(text, x, y) {
         @Override
         protected Hud.Element getElement() {
            return renderer.getElement();
         }
      };

      setPosition(x, y);
   }

   protected TextBoxElement(Component text, float x, float y) {
      this(StyledText.fromComponent(text), x, y);
   }

   protected TextBoxElement(String string, float x, float y) {
      this(StyledText.fromString(string), x, y);
   }

   public This setText(StyledText component) {
      text.setText(component);

      return getThis();
   }

   public This setText(Component component) {
      return setText(StyledText.fromComponent(component));
   }

   public This setText(String string) {
      return setText(StyledText.fromString(string));
   }

   public This setTextStyle(TextShadow style) {
      text.setStyle(style);

      return getThis();
   }

   public This setTextColor(int r, int g, int b, int a) {
      text.setColor(r, g, b, a);

      return getThis();
   }

   public This setTextColor(int rgba) {
      text.setColor(rgba);

      return getThis();
   }


   public This setTextColor(Color color) {
      return setTextColor(color.getColor());
   }

   public This setMaxWidth(float width) {
      text.setMaxWidth(width);

      return getThis();
   }

   public This setHorizontalAlignment(HorizontalAlignment align) {
      text.setHorizontalAlignment(align);

      return getThis();
   }

   public This setVerticalAlignment(VerticalAlignment align) {
      text.setVerticalAlignment(align);

      return getThis();
   }

   public This setAlignment(HorizontalAlignment hor, VerticalAlignment vert) {
      return setHorizontalAlignment(hor).setVerticalAlignment(vert);
   }

   public This setAlignment(Hud.Element element) {
      return setHorizontalAlignment(element.getHorizontalAlignment()).setVerticalAlignment(element.getVerticalAlignment());
   }

   public This setFill(int r, int g, int b, int a) {
      return setFill(Color.ofRGBA(r, g, b, a));
   }

   public This setFill(int rgba) {
      return setFill(Color.ofTransparent(rgba));
   }


   public This setFill(Color color) {
      rect.setFill(color);

      return getThis();
   }

   public This setGradient(Color from, Color to) {
      rect.setGradient(from, to);

      return getThis();
   }

   protected abstract Hud.Element getElement();

   @Override
   public This setPosition(float x, float y) {
      rect.setPosition(x, y);

      return getThis();
   }

   @Override
   public float getX() {
      return rect.getX();
   }

   @Override
   public This setX(float x) {
      rect.setX(x);

      return getThis();
   }

   @Override
   public float getY() {
      return rect.getY();
   }

   @Override
   public This setY(float y) {
      rect.setY(y);

      return getThis();
   }

   @Override
   public float getHeight() {
      return rect.getHeight();
   }

   @Override
   public This setHeight(float height) {
      rect.setHeight(height);

      return getThis();
   }

   @Override
   public float getWidth() {
      return rect.getWidth();
   }

   @Override
   public This setWidth(float width) {
      rect.setWidth(width);

      return getThis();
   }

   @Override
   public float getScale() {
      return text.getScale();
   }

   @Override
   public This setScale(float scale) {
      text.setScale(scale);

      return getThis();
   }

   public This setPadding(float left, float top, float right, float bottom) {
      padding.left = left;
      padding.top = top;
      padding.right = right;
      padding.bottom = bottom;

      return getThis();
   }

   public This setPaddingLeft(float padding) {
      this.padding.left = padding;

      return getThis();
   }

   public This setPaddingTop(float padding) {
      this.padding.top = padding;

      return getThis();
   }

   public This setPaddingRight(float padding) {
      this.padding.right = padding;

      return getThis();
   }

   public This setPaddingBottom(float padding) {
      this.padding.bottom = padding;

      return getThis();
   }

   public This dynamic() {
      dynamic = true;

      return getThis();
   }

   @Override
   public This with(Hud.Element element) {
      return super.with(element).setAlignment(element);
   }

   @Override
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
      float textX = getX();
      float textY = getY();
      float textWidth = getWidth();
      float textHeight = getHeight();

      if (dynamic) {
         float width = FontRenderer.getWidth(text.text, text.maxWidth) + padding.left + padding.right;
         float height = FontRenderer.getHeight(text.text, text.maxWidth) + padding.top + padding.bottom;

         float x = switch (text.horizontalAlignment) {
            case LEFT -> getX();
            case CENTER -> getX() + (getWidth() / 2) - (width / 2);
            case RIGHT -> getX() + getWidth() - width;
         };

         float y = switch (text.verticalAlignment) {
            case TOP -> getY();
            case MIDDLE -> getY() + (getHeight() / 2) - (height / 2);
            case BOTTOM -> getY() + getHeight() - height;
         };

         textY = y + (padding.top * 0.25F);
         textHeight = height;

         textWidth = width;
         textX = switch(text.horizontalAlignment) {
            case LEFT -> x;
            case CENTER -> x + (padding.left * 0.25F);
            case RIGHT -> padding.left + x;
         };


         setPosition(x, y).setSize(width, height).setVerticalAlignment(VerticalAlignment.TOP);
      }

      rect.render(poseStack, bufferSource, partialTicks, window);

      Renderer.text(
              poseStack,
              bufferSource,
              text.text,
              (int) textX + padding.left,
              (int) textX + textWidth - padding.right - padding.left,
              (int) textY + padding.top,
              (int) textY + textHeight - padding.bottom - padding.top,
              Math.max(0, text.maxWidth - padding.right),
              text.color,
              text.horizontalAlignment,
              text.verticalAlignment,
              text.style,
              getScale()
      );
   }
}
