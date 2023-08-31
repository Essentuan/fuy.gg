package com.busted_moments.client.features.war;

import com.busted_moments.client.models.war.Tower;
import com.busted_moments.client.models.war.WarModel;
import com.busted_moments.client.models.war.events.TowerDamagedEvent;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.heartbeat.annotations.Schedule;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.busted_moments.core.util.NumUtil;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static net.minecraft.ChatFormatting.*;

@Default(State.ENABLED)
@Config.Category("War")
@Feature.Definition(name = "War Info Overlay")
public class WarInfoOverlayFeature extends Feature {
   @Hud
   private static WarInfo HUD;

   @Value("Text Style")
   private static TextShadow style = TextShadow.OUTLINE;

   @Alpha
   @Value("Background Color")
   private static Color background_color = Color.ofRGBA(0, 0, 0, 127);

   @Hud.Name("War Info")
   @Hud.Offset(x = 0.0F, y = 7.644165F)
   @Hud.Size(width = 205.0F, height = 22.423138F)
   @Hud.Anchor(OverlayPosition.AnchorSection.MIDDLE_LEFT)
   @Hud.Align(vertical = VerticalAlignment.TOP, horizontal = HorizontalAlignment.LEFT)
   private static class WarInfo extends Hud.Element {
      @Override
      protected void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         if (WarModel.current().map(war -> !war.hasStarted()).orElse(true)) return;

         render(x, y, TIME_IN_WAR,
                 TOWER_EHP,
                 DPS_MIN,
                 DPS_MAX,
                 DPS_1_SECOND,
                 DPS_5_SECONDS,
                 DPS_TOTAL,
                 TIME_REMAINING
         );
      }

      @Override
      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(x, y,
                 Duration.of(224, TimeUnit.SECONDS),
                 BigDecimal.valueOf(12523563),
                 BigDecimal.valueOf(48800),
                 BigDecimal.valueOf(72000),
                 BigDecimal.valueOf(0),
                 BigDecimal.valueOf(0),
                 BigDecimal.valueOf(0),
                 Duration.of(104, TimeUnit.SECONDS)
         );
      }

      private void render(float x,
                          float y,
                          Duration inWar,
                          BigDecimal ehp,
                          BigDecimal dpsMin,
                          BigDecimal dpsMax,
                          BigDecimal dps1Second,
                          BigDecimal dps5Seconds,
                          BigDecimal dpsOverall,
                          Duration timeRemaining
      ) {
         new TextBox(TextBuilder.of("War Info ", AQUA)
                 .append("[", DARK_AQUA)
                 .append(WarCommon.format(inWar), RESET, DARK_AQUA)
                 .append("]", DARK_AQUA).line()
                 .append("Tower EHP: ", RESET, WHITE)
                 .append(NumUtil.truncate(ehp), AQUA).line()
                 .append("Tower DPS: ", RESET, WHITE)
                 .append(NumUtil.truncate(dpsMin), AQUA)
                 .append("-", GRAY)
                 .append(NumUtil.truncate(dpsMax), AQUA).line()
                 .append("\nTeam DPS/1s: ", RESET, WHITE)
                 .append(NumUtil.truncate(dps1Second), RED).line()
                 .append("Team DPS/5s: ", RESET, WHITE)
                 .append(NumUtil.truncate(dps5Seconds), RED).line()
                 .append("Team DPS (total): ", RESET, WHITE)
                 .append(NumUtil.truncate(dpsOverall), YELLOW).line()
                 .append("\nEstimated Time Remaining: ", RESET, WHITE)
                 .append(WarCommon.format(timeRemaining), GREEN), x, y).setTextStyle(style)
                 .setFill(background_color)
                 .with(this)
                 .setPadding(5, 5, 5, 5)
                 .setMaxWidth(getWidth())
                 .dynamic()
                 .build();
      }
   }

   private static Duration TIME_IN_WAR = Duration.of(0, TimeUnit.SECONDS);
   private static BigDecimal TOWER_EHP = new BigDecimal(0);
   private static BigDecimal DPS_MIN = new BigDecimal(0);
   private static BigDecimal DPS_MAX = new BigDecimal(0);
   private static BigDecimal DPS_1_SECOND = new BigDecimal(0);
   private static BigDecimal DPS_5_SECONDS = new BigDecimal(0);
   private static BigDecimal DPS_TOTAL = new BigDecimal(0);
   private static Duration TIME_REMAINING = Duration.FOREVER;

   @SubscribeEvent
   public void onTowerDamage(TowerDamagedEvent event) {
      Tower.Stats tower = event.getAfter();

      TOWER_EHP = BigDecimal.valueOf(tower.ehp());

      double ATTACK_SPEED = tower.attackSpeed();

      DPS_MIN = BigDecimal.valueOf(tower.damageMin() * ATTACK_SPEED * 2);
      DPS_MAX = BigDecimal.valueOf(tower.damageMax() * ATTACK_SPEED * 2);
   }

   @Schedule(rate = 250, unit = TimeUnit.MILLISECONDS)
   private void onUpdate() {
      WarModel.current().ifPresent(war -> {
         if (!war.hasStarted()) return;

         TIME_IN_WAR = war.getDuration();

         DPS_1_SECOND = BigDecimal.valueOf(war.getDPS(1, TimeUnit.SECONDS));
         DPS_5_SECONDS = BigDecimal.valueOf(war.getDPS(5, TimeUnit.SECONDS));
         DPS_TOTAL = BigDecimal.valueOf(war.getDPS(Duration.FOREVER));

         if (DPS_TOTAL.signum() == 0) TIME_REMAINING = Duration.FOREVER;
         else TIME_REMAINING = Duration.of(TOWER_EHP.divide(DPS_TOTAL, RoundingMode.DOWN), TimeUnit.SECONDS);
      });
   }
}
