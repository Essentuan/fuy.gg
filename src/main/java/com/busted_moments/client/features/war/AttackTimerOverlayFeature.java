package com.busted_moments.client.features.war;

import com.busted_moments.client.models.war.Defense;
import com.busted_moments.client.models.territory.TerritoryModel;
import com.busted_moments.client.models.war.timer.Timer;
import com.busted_moments.client.models.war.timer.TimerModel;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.ChronoUnit;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.handlers.scoreboard.event.ScoreboardSegmentAdditionEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.territories.GuildAttackScoreboardPart;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

import static net.minecraft.ChatFormatting.*;

@Default(State.ENABLED)
@Config.Category("War")
@Feature.Definition(name = "Attack Timer Overlay", description = "Shows all active timers")
public class AttackTimerOverlayFeature extends Feature {
   @Hud
   private static TimerOverlay HUD;

   @Value("Hide timers on Scoreboard")
   @Tooltip("Hides the regular timers on the scoreboard")
   private static boolean hideTimers = true;

   @Value("Text Style")
   private static TextShadow style = TextShadow.OUTLINE;

   @Alpha
   @Value("Background Color")
   private static Color background_color = Color.ofRGBA(0, 0, 0, 127);

   private static List<Timer> ACTIVE_TIMERS = List.of();

   @SubscribeEvent()
   public void onScoreboardSegmentChange(ScoreboardSegmentAdditionEvent event) {
      if (hideTimers && event.getSegment().getScoreboardPart() instanceof GuildAttackScoreboardPart) event.setCanceled(true);
   }

   @Hud.Name("Attack Timer Overlay")
   @Hud.Offset(x = 0F, y = 60F)
   @Hud.Size(width = 270F, height = 25.926636F)
   @Hud.Anchor(OverlayPosition.AnchorSection.TOP_RIGHT)
   @Hud.Align(vertical = VerticalAlignment.TOP, horizontal = HorizontalAlignment.RIGHT)
   private static class TimerOverlay extends Hud.Element {
      @Override
      protected void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(ACTIVE_TIMERS, x, y);
      }

      @Override
      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(List.of(
                 new Timer("Mine Base Plains", Duration.of(5, ChronoUnit.MINUTES).add(40, ChronoUnit.SECONDS), Defense.VERY_HIGH),
                 new Timer("Almuj City", Duration.of(5, ChronoUnit.MINUTES).add(13, ChronoUnit.SECONDS), Defense.HIGH),
                 new Timer("Detlas", Duration.of(3, ChronoUnit.MINUTES).add(25, ChronoUnit.SECONDS), Defense.HIGH),
                 new Timer("Detlas Savannah Transition", Duration.of(2, ChronoUnit.MINUTES).add(47, ChronoUnit.SECONDS), Defense.HIGH)
         ), x, y);
      }

      private void render(List<Timer> timers, float x, float y) {
         if (timers.isEmpty()) return;

         new TextBox(builder -> builder.append(timers, timer -> builder
                 .append(timer.getTerritory(), getColor(timer))
                 .append(" (", RESET, GOLD)
                 .append(timer.getDefense().toText(timer.isConfident()))
                 .append(")", RESET, GOLD)
                 .append(": ", GOLD)
                 .append(format(timer.getRemaining()), AQUA)), x, y).setTextStyle(style)
                 .setFill(background_color)
                 .with(this)
                 .setPadding(5, 5, 5, 5)
                 .setMaxWidth(getWidth())
                 .dynamic()
                 .build();
      }

      private static ChatFormatting[] getColor(Timer timer) {
         return TerritoryModel.getCurrentTerritory()
                 .filter(territory -> territory.getName().equals(timer.getTerritory()))
                 .map(territory -> new ChatFormatting[]{LIGHT_PURPLE, BOLD})
                 .orElseGet(() -> new ChatFormatting[]{GOLD});
      }

      private static String format(Duration duration) {
         int minutes = (int) duration.getPart(ChronoUnit.MINUTES);
         int seconds = (int) duration.getPart(ChronoUnit.SECONDS);

         return ((minutes < 10) ? "0" + minutes : Integer.toString(minutes)) + ":" + ((seconds < 10) ? "0" + seconds : Integer.toString(seconds));
      }
   }

   @SubscribeEvent
   private static void onTick(TickEvent event) {
      ACTIVE_TIMERS = TimerModel.getTimers()
              .stream()
              .sorted()
              .toList();
   }
}
