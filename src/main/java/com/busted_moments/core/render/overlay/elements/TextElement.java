package com.busted_moments.core.render.overlay.elements;

import com.busted_moments.core.render.FontRenderer;
import com.busted_moments.core.render.Renderer;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.render.overlay.Overlays;
import com.busted_moments.core.text.TextBuilder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.colors.CommonColors;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

public abstract class TextElement<This extends TextElement<This>> extends Overlays.Element<This> {
   public StyledText text;
   public TextShadow style = TextShadow.OUTLINE;
   public CustomColor color = CommonColors.WHITE;
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

   public This setSize(float width, float height) {
      this.width = width;
      this.height = height;

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
   public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
      Renderer.text(
              poseStack,
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
              getScale()
      );
   }
}
