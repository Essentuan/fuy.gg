package com.busted_moments.client.features.war;

import com.busted_moments.client.models.war.events.WarStartEvent;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.util.NumUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Managers;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Default(State.ENABLED)
@Config.Category("War")
@Feature.Definition(name = "Display Weekly War Count")
public class WeeklyWarCountOverlay extends Feature {
   private static final Duration WEEK = Duration.of(1, ChronoUnit.WEEKS);

   @Hud
   private static WeeklyWars HUD;

   @Value("Text Style")
   private static TextShadow style = TextShadow.OUTLINE;

   @Value("Text Color")
   private static Color text_color = ChatUtil.colorOf(ChatFormatting.LIGHT_PURPLE);

   @Alpha
   @Value("Background Color")
   private static Color background_color = Color.ofRGBA(0, 0, 0, 127);

   @Hidden("wars")
   private static List<Date> WARS = new ArrayList<>();

   private static int WEEKLY_WARS = 0;

   @SubscribeEvent
   private static void onWarStart(WarStartEvent event) {
      Managers.TickScheduler.scheduleNextTick(() -> WARS.add(new Date()));
   }

   @SubscribeEvent
   private static void onTick(TickEvent event) {
      WEEKLY_WARS = (int) WARS.stream()
              .filter(date -> Duration.since(date).lessThan(WEEK))
              .count();
   }

   @Hud.Name("Weekly Wars")
   @Hud.Offset(x = -250.0F, y = 0F)
   @Hud.Size(width = 91.5F, height = 14.415209F)
   @Hud.Anchor(OverlayPosition.AnchorSection.BOTTOM_MIDDLE)
   @Hud.Align(vertical = VerticalAlignment.BOTTOM, horizontal = HorizontalAlignment.RIGHT)
   private static class WeeklyWars extends Hud.Element {
      @Override
      protected void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(x, y, WEEKLY_WARS);
      }

      @Override
      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(x, y, 13);
      }

      private void render(float x, float y, int wars) {
         if (wars == 0) return;

         new TextBox(builder -> builder
                 .append(NumUtil.format(wars))
                 .append(" War" + ((wars != 1) ? "s" : "")), x, y)
                 .setTextColor(text_color)
                 .setTextStyle(style)
                 .setFill(background_color)
                 .with(this)
                 .setPadding(5, 4, 5, 4)
                 .setMaxWidth(getWidth())
                 .dynamic()
                 .build();
      }
   }

   public static List<Date> getWars() {
      return WARS;
   }
}
