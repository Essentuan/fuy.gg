package com.busted_moments.core.render.overlay;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.annotated.Annotated;
import com.busted_moments.core.artemis.FuyFeature;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.render.RenderableElement;
import com.busted_moments.core.render.overlay.elements.TextElement;
import com.busted_moments.core.render.overlay.elements.RectElement;
import com.busted_moments.core.render.overlay.elements.textbox.TextBoxElement;
import com.busted_moments.core.text.TextBuilder;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.core.text.StyledText;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;

import java.lang.annotation.*;
import java.util.Deque;
import java.util.LinkedList;
import java.util.function.Consumer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Hud {
   abstract class Element extends Config {
      final Deque<RenderableElement<?>> elements = new LinkedList<>();

      final FuyFeature.Overlay overlay;
      final boolean enabledByDefault;

      private Feature feature;

      @Hidden("Was Enabled")
      private boolean wasEnabled = true;

      public Element() {
         super(
                 Annotated.Required(Name.class),
                 Annotated.Optional(new Size.Impl(100, 100)),
                 Annotated.Optional(new Offset.Impl(0, 0)),
                 Annotated.Optional(new Align.Impl(VerticalAlignment.MIDDLE, HorizontalAlignment.CENTER)),
                 Annotated.Optional(new Anchor.Impl(OverlayPosition.AnchorSection.MIDDLE)),
                 Annotated.Optional(new Default.Impl(State.ENABLED))
         );

         Size size = getAnnotation(Size.class);
         Offset offset = getAnnotation(Offset.class);
         Align align = getAnnotation(Align.class);
         Anchor anchor = getAnnotation(Anchor.class);
         enabledByDefault = getAnnotation(Default.class, Default::value).asBoolean();

         final Element element = this;

         overlay = new FuyFeature.Overlay(
            getAnnotation(Name.class, Name::value), this,
                 size.width(),
                 size.height(), 
                 offset.y(),
                 offset.x(),
                 align.vertical(),
                 align.horizontal(),
                 anchor.value()
         );

         Overlays.overlays.add(this);
      }

      public void enable() {
         if (wasEnabled) Managers.Overlay.enableOverlay(overlay);
      }

      public void disable() {
         wasEnabled = overlay.isEnabled();

         Managers.Overlay.disableOverlay(overlay);
      }

      public boolean isEnabled() {
         return overlay.isEnabled();
      }

      public Hud.Element setFeature(Feature feature) {
         this.feature = feature;

         return this;
      }

      public Feature getFeature() {
         return feature;
      }

      public float getX() {
         return overlay.getRenderX();
      }

      public float getY() {
         return overlay.getRenderY();
      }

      public float getHeight() {
         return overlay.getHeight();
      }

      public float getWidth() {
         return overlay.getWidth();
      }

      public HorizontalAlignment getHorizontalAlignment() {
         return overlay.getRenderHorizontalAlignment();
      }

      public VerticalAlignment getVerticalAlignment() {
         return overlay.getRenderVerticalAlignment();
      }


      public void render(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
         elements.clear();

         if (!Models.WorldState.onWorld()) return;

         onRender(
                 getX(),
                 getY(),
                 getWidth(),
                 getHeight(),
                 poseStack, partialTicks, window
         );

         elements.forEach(e -> e.render(poseStack, bufferSource, partialTicks, window));
      }

      public void renderPreview(PoseStack poseStack, MultiBufferSource bufferSource, float partialTicks, Window window) {
         elements.clear();

         if (!Models.WorldState.onWorld()) return;

         onRenderPreview(
                 getX(),
                 getY(),
                 getWidth(),
                 getHeight(),
                 poseStack, partialTicks, window
         );

         elements.forEach(e -> e.render(poseStack, bufferSource, partialTicks, window));
      }

      protected abstract void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window);

      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         onRender(x, y, width, height, poseStack, partialTicks, window);
      }

      public class Text extends TextElement<Text> {
         public Text(StyledText text, float x, float y) {
            super(text, x, y);
         }

         public Text(TextBuilder text, float x, float y) {
            super(text.build(), x, y);
         }


         public Text(Component text, float x, float y) {
            super(text, x, y);
         }

         public Text(String string, float x, float y) {
            super(string, x, y);
         }

         @Override
         protected Hud.Element getElement() {
            return Hud.Element.this;
         }
      };

      public class TextBox extends TextBoxElement<TextBox> {
         public TextBox(StyledText text, float x, float y) {
            super(text, x, y);
         }

         public TextBox(Consumer<TextBuilder> text, float x, float y) {
            this(create(text), x, y);
         }

         private static TextBuilder create(Consumer<TextBuilder> text) {
            TextBuilder builder = TextBuilder.empty();

            text.accept(builder);

            return builder;
         }

         public TextBox(TextBuilder text, float x, float y) {
            super(text.build(), x, y);
         }

         public TextBox(Component text, float x, float y) {
            super(text, x, y);
         }

         public TextBox(String string, float x, float y) {
            super(string, x, y);
         }

         @Override
         protected Hud.Element getElement() {
            return Hud.Element.this;
         }
      }

      public class Rect extends RectElement<Rect> {
         @Override
         protected Hud.Element getElement() {
            return Hud.Element.this;
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @interface Name {
      String value();
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @interface Size {
      float width();
      float height();

      @SuppressWarnings("ClassExplicitlyAnnotation")
      record Impl(float width, float height) implements Size {
         @Override
         public float width() {
            return width;
         }

         @Override
         public float height() {
            return height;
         }

         @Override
         public Class<? extends Annotation> annotationType() {
            return Size.class;
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @interface Offset {
      float x();
      float y();

      @SuppressWarnings("ClassExplicitlyAnnotation")
      record Impl(float x, float y) implements Offset {
         @Override
         public float x() {
            return x;
         }

         @Override
         public float y() {
            return y;
         }

         @Override
         public Class<? extends Annotation> annotationType() {
            return Offset.class;
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @interface Align {
      VerticalAlignment vertical();
      HorizontalAlignment horizontal();

      @SuppressWarnings("ClassExplicitlyAnnotation")
      record Impl(VerticalAlignment vertical, HorizontalAlignment horizontal) implements Align {
         @Override
         public VerticalAlignment vertical() {
            return vertical;
         }

         @Override
         public HorizontalAlignment horizontal() {
            return horizontal;
         }

         @Override
         public Class<? extends Annotation> annotationType() {
            return Align.class;
         }
      }
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Target(ElementType.TYPE)
   @interface Anchor {
      OverlayPosition.AnchorSection value();

      @SuppressWarnings("ClassExplicitlyAnnotation")
      record Impl(OverlayPosition.AnchorSection anchorSection) implements Anchor {
         @Override
         public OverlayPosition.AnchorSection value() {
            return anchorSection;
         }

         @Override
         public Class<? extends Annotation> annotationType() {
            return Anchor.class;
         }
      }
   }
}
