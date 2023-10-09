package com.busted_moments.client.features.raids;

import com.busted_moments.client.models.raids.Raid;
import com.busted_moments.client.models.raids.RaidModel;
import com.busted_moments.client.models.raids.RaidType;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.render.overlay.Hud;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;

@Default(State.ENABLED)
@Config.Category("Raids")
@Feature.Definition(name = "Raid Overlay")
public class RaidOverlayFeature extends Feature {
   @Hud
   private static RaidOverlay HUD;

   @Value("Text Style")
   private static TextShadow style = TextShadow.OUTLINE;

   @Alpha
   @Value("Background Color")
   private static Color background_color = Color.ofRGBA(0, 0, 0, 127);

   @Hud.Name("Raid Overlay")
   @Hud.Offset(x = 0.0F, y = 7.644165F)
   @Hud.Size(width = 205.0F, height = 22.423138F)
   @Hud.Anchor(OverlayPosition.AnchorSection.MIDDLE_LEFT)
   @Hud.Align(vertical = VerticalAlignment.TOP, horizontal = HorizontalAlignment.LEFT)
   public static class RaidOverlay extends Hud.Element {
      @Override
      protected void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(x, y, RaidModel.current().orElse(null));
      }

      private static final Raid PREVIEW = new Raid(RaidType.THE_CANYON_COLOSSUS);

      @Override
      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(x, y, PREVIEW);
      }

      private void render(float x, float y, Raid raid) {
         if (raid == null) return;

         new TextBox(Raid.format(raid), x, y)
                 .setTextStyle(style)
                 .setFill(background_color)
                 .with(this)
                 .setPadding(5, 5, 5, 5)
                 .setMaxWidth(getWidth())
                 .dynamic()
                 .build();
      }
   }
}
