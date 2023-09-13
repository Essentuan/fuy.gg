package com.busted_moments.client.features;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.api.requests.serverlist.ServerList;
import com.busted_moments.core.api.requests.serverlist.World;
import com.busted_moments.core.heartbeat.annotations.Schedule;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.FormatFlag;
import com.busted_moments.core.time.TimeUnit;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.components.Models;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;

import java.util.Comparator;

import static com.wynntils.utils.mc.McUtils.mc;
import static com.wynntils.utils.mc.McUtils.player;

@Default(State.ENABLED)
@Feature.Definition(name = "Server Info Overlay")
public class ServerInfoFeature extends Feature {
   @Hud
   private static ServerInfo HUD;

   @Value("Text Style")
   private static TextShadow style = TextShadow.OUTLINE;

   @Alpha
   @Value("Background Color")
   private static Color background_color = Color.ofRGBA(0, 0, 0, 127);

   private static World CURRENT_WORLD;
   private static World NEWEST_WORLD;

   @Hud.Name("Server Info Overlay")
   @Hud.Offset(x = 0F, y = 270F)
   @Hud.Size(width = 385.5F, height = 16.917747F)
   @Hud.Anchor(OverlayPosition.AnchorSection.TOP_LEFT)
   @Hud.Align(vertical = VerticalAlignment.TOP, horizontal = HorizontalAlignment.LEFT)
   private static class ServerInfo extends Hud.Element {
      @Override
      protected void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         if (mc().options.keyPlayerList.isDown) render(x, y);
      }

      @Override
      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(x, y);
      }

      private void render(float x, float y) {
         if (CURRENT_WORLD == null && NEWEST_WORLD == null) return;

         TextBuilder builder = TextBuilder.empty();

         if (CURRENT_WORLD != null) {
            Duration uptime = CURRENT_WORLD.getUptime();

            builder
                    .append("Your World (", ChatFormatting.WHITE)
                    .append(CURRENT_WORLD.getWorld(), ChatFormatting.AQUA)
                    .append("): ", ChatFormatting.WHITE)
                    .append(uptime.lessThan(1, TimeUnit.MINUTES) ?
                            uptime.toString(FormatFlag.COMPACT, TimeUnit.SECONDS) :
                            uptime.toString(FormatFlag.COMPACT, TimeUnit.MINUTES),
                            ChatFormatting.WHITE
                    );
         }

         if (NEWEST_WORLD != null) {
            if (CURRENT_WORLD != null) builder.line();

            Duration uptime = NEWEST_WORLD.getUptime();

            builder.append("Newest World (", ChatFormatting.WHITE)
                    .append(NEWEST_WORLD.getWorld(), ChatFormatting.AQUA)
                    .append("): ", ChatFormatting.WHITE)
                    .append(uptime.lessThan(1, TimeUnit.MINUTES) ?
                            uptime.toString(FormatFlag.COMPACT, TimeUnit.SECONDS) :
                            uptime.toString(FormatFlag.COMPACT, TimeUnit.MINUTES),
                            ChatFormatting.WHITE
                    );
         }

         new TextBox(builder, x, y)
                 .setTextStyle(style)
                 .setFill(background_color)
                 .with(this)
                 .setPadding(5, 5, 5, 5)
                 .setMaxWidth(getWidth())
                 .dynamic()
                 .build();
      }
   }

   @Schedule(rate = 10, unit = TimeUnit.SECONDS)
   private void updateWorldInfo() {
      new ServerList.Request().thenAccept(optional -> optional.ifPresent(servers -> {
         if (!Models.WorldState.onWorld()) CURRENT_WORLD = null;
         else if (servers.contains(Models.WorldState.getCurrentWorldName())) {
            CURRENT_WORLD = servers.get(Models.WorldState.getCurrentWorldName());
         } else CURRENT_WORLD = servers.findPlayer(player().getGameProfile().getName());

         servers.stream().min(Comparator.comparing(World::getUptime)).ifPresentOrElse(
                 world -> NEWEST_WORLD = world,
                 () -> NEWEST_WORLD = null
         );
      }));
   }
}
