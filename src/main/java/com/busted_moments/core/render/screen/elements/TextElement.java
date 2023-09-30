package com.busted_moments.core.render.screen.elements;

import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.render.screen.Screen;
import com.busted_moments.core.render.screen.ScreenElement;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public abstract class TextElement<This extends TextElement<This>> extends ScreenElement.Sizable<This> {
   public StyledText text;
   public TextShadow style = TextShadow.OUTLINE;
   public CustomColor color = CommonColors.WHITE;

   public float width = 100;
   public float height = 100;

   public float maxWidth = 0;

   public VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
   public HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

   protected TextElement(StyledText text, float x, float y) {
      setText(text);

      this.x = x;
      this.y = y;
   }

   protected TextElement(Component text, float x, float y) {
      this(StyledText.fromComponent(text), x, y);
   }

   protected TextElement(String string, float x, float y) {
      this(StyledText.fromString(string), x, y);
   }

   public This setText(StyledText component) {
      this.text = component;

      return getThis();
   }

   public This setText(TextBuilder builder) {
      return setText(builder.build());
   }

   public This setText(Component component) {
      return setText(StyledText.fromComponent(component));
   }

   public This setText(String string) {
      return setText(StyledText.fromString(string));
   }

   public This setStyle(TextShadow style) {
      this.style = style;

      return getThis();
   }

   public This setColor(int r, int g, int b, int a) {
      this.color = new CustomColor(r, g, b, a);

      return getThis();
   }

   public This setColor(int rgba) {
      this.color = CustomColor.fromInt(rgba);

      return getThis();
   }


   public This setColor(Color color) {
      return setColor(color.getColor());
   }


   public This setColor(CustomColor color) {
      this.color = color;

      return getThis();
   }

   public This setMaxWidth(float width) {
      this.maxWidth = width;

      return getThis();
   }

   public This setHorizontalAlignment(HorizontalAlignment align) {
      this.horizontalAlignment = align;

      return getThis();
   }

   public This setVerticalAlignment(VerticalAlignment align) {
      this.verticalAlignment = align;

      return getThis();
   }

   public This setAlignment(HorizontalAlignment hor, VerticalAlignment vert) {
      return setHorizontalAlignment(hor).setVerticalAlignment(vert);
   }

   public This setAlignment(Hud.Element element) {
      return setHorizontalAlignment(element.getHorizontalAlignment()).setVerticalAlignment(element.getVerticalAlignment());
   }

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
   public float getHeight() {
      return height;
   }

   @Override
   public This setHeight(float height) {
      this.height = height;

      return getThis();
   }

   @Override
   public This center() {
      Screen.Element element = getElement();

      return setX((element.width/2F) - FontRenderer.getWidth(text, maxWidth)/2).setY((element.height/2F) - FontRenderer.getHeight(text, maxWidth)/2);
   }

   @Override
   public void render(@NotNull GuiGraphics graphics, MultiBufferSource.BufferSource bufferSource, int mouseX, int mouseY, float partialTick) {
      Renderer.text(
              graphics.pose(),
              bufferSource,
              text,
              getX(),
              getX() + getWidth(),
              getY(),
              getY() + getHeight(),
              maxWidth,
              color,
              horizontalAlignment,
              verticalAlignment,
              style,
              1
      );
   }
}
