package com.busted_moments.core.render;

import com.busted_moments.client.util.ChatUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.text.StyledText;
import com.wynntils.core.text.StyledTextPart;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.colors.CustomColor;
import com.wynntils.utils.mc.ComponentUtils;
import com.wynntils.utils.render.buffered.BufferedFontRenderer;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class FontRenderer {
   private static final Splitter splitter = new Splitter(1000);

   public static class Splitter {
      private final Map<Integer, List<StyledText>> cache = new ConcurrentHashMap<>();
      private final int maxEntries;

      public Splitter(int maxEntries) {
         this.maxEntries = maxEntries;
      }

      private int getKey(StyledText text, int maxWidth) {
         return Objects.hash(text, maxWidth);
      }

      private List<StyledText> compute(StyledText text, int maxWidth, int key) {
         if (maxWidth == 0) {
            cache.put(key, List.of(text.split("\n")));
            return cache.get(key);
         }

         final StyledText[] lastPart = {StyledText.EMPTY};

         cache.put(key, renderer().getFont().getSplitter().splitLines(text.getComponent(), maxWidth, Style.EMPTY).stream()
                 .map(ChatUtil::toStyledText)
                 .flatMap(styled -> Stream.of(styled.split("\n")))
                 .map(styled -> {
                    Style lastStyle = ComponentUtils.getLastPartCodes(lastPart[0]);
                    lastPart[0] = StyledText.fromPart(new StyledTextPart("", lastStyle, null, null)).append(styled);

                    return lastPart[0];
                 }).toList());

         return cache.get(key);
      }

      public List<StyledText> split(StyledText text, int maxWidth) {
         int key = getKey(text, maxWidth);

         List<StyledText> result;

         if (cache.containsKey(key)) result = cache.get(key);
         else result = compute(text, maxWidth, key);

         if (cache.size() > maxEntries) {
            cache.clear();
         }

         return result;
      }

      public void clear() {
         cache.clear();
      }
   }

   static void draw(PoseStack poseStack,
                           MultiBufferSource bufferSource,
                           StyledText text,
                           float x1,
                           float x2,
                           float y1,
                           float y2,
                           float maxWidth,
                           CustomColor customColor,
                           HorizontalAlignment horizontalAlignment,
                           VerticalAlignment verticalAlignment,
                           TextShadow textShadow,
                           float textScale
   ) {
      float renderX = switch (horizontalAlignment) {
         case LEFT -> x1;
         case CENTER -> (x1 + x2) / 2f;
         case RIGHT -> x2;
      };

      float renderY = switch (verticalAlignment) {
         case TOP -> y1;
         case MIDDLE -> (y1 + y2) / 2f;
         case BOTTOM -> y2;
      };

      renderText(poseStack, bufferSource, text, renderX, renderY, maxWidth, customColor, horizontalAlignment, verticalAlignment, textShadow, textScale);
   }

   private static BufferedFontRenderer renderer() {
      return BufferedFontRenderer.getInstance();
   }

   public static Font font() {
      return renderer().getFont();
   }

   public static List<StyledText> split(StyledText text, int maxWidth) {
      return splitter.split(text, maxWidth);
   }

   //This sucks ass but should use the minimum amount with loops (and should be faster than built in artemis methods?)
   private static void renderText(
           PoseStack poseStack,
           MultiBufferSource bufferSource,
           StyledText text,
           float x,
           float y,
           float maxWidth,
           CustomColor customColor,
           HorizontalAlignment horizontalAlignment,
           VerticalAlignment verticalAlignment,
           TextShadow shadow,
           float textScale
   ) {
      if (text == null) return;

      final int[] lines = {0};

      split(text, (int) maxWidth).forEach(styled -> {
         renderer().renderText(
                 poseStack,
                 bufferSource,
                 styled,
                 x,
                 y + (lines[0] * renderer().getFont().lineHeight * textScale),
                 customColor,
                 horizontalAlignment,
                 verticalAlignment,
                 shadow,
                 textScale
         );

         lines[0]++;
      });
   }

   public static float getWidth(StyledText styled, float maxWidth) {
      return split(styled, (int) maxWidth).stream()
              .map(FontRenderer::getWidth)
              .max(Float::compare).orElse(0f);
   }

   public static float getHeight(StyledText text, float maxWidth) {
      return split(text, (int) maxWidth).size() * renderer().getFont().lineHeight;
   }

   public static float getWidth(String text) {
      return renderer().getFont().width(text);
   }


   public static float getWidth(StyledText text) {
      return getWidth(text.getComponent());
   }

   public static float getWidth(Component text) {
      return renderer().getFont().width(text);
   }

   public static int lineHeight() {
      return renderer().getFont().lineHeight;
   }

   @SubscribeEvent
   private static void onTick(TickEvent event) {
      splitter.clear();
   }
}
