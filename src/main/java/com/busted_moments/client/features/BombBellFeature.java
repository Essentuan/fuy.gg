package com.busted_moments.client.features;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.util.Comparing;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import com.wynntils.core.consumers.overlays.OverlayPosition;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.type.BombInfo;
import com.wynntils.models.worlds.type.BombType;
import com.wynntils.utils.render.type.HorizontalAlignment;
import com.wynntils.utils.render.type.TextShadow;
import com.wynntils.utils.render.type.VerticalAlignment;
import me.shedaniel.math.Color;
import net.minecraft.ChatFormatting;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.List;

import static com.wynntils.core.components.Models.Bomb;

@Default(State.ENABLED)
@Feature.Definition(name = "Bomb Bell Overlay", description = "")
public class BombBellFeature extends Feature {
   @Hud
   private static BombBell HUD;

   @Value("Display bomb thrower")
   @Tooltip("Show the player who threw a bomb")
   private static boolean display_bomb_thrower = true;

   @Value("Text Style")
   private static TextShadow style = TextShadow.OUTLINE;

   @Value("Text Color")
   private static Color text_color = ChatUtil.colorOf(ChatFormatting.YELLOW);

   @Alpha
   @Value("Background Color")
   private static Color background_color = Color.ofRGBA(0, 0, 0, 127);

   private static List<BombInfo> ACTIVE_BOMBS = List.of();

   @Hud.Name("Bomb Bell Overlay")
   @Hud.Offset(x = 0F, y = 0F)
   @Hud.Size(width = 385.5F, height = 16.917747F)
   @Hud.Anchor(OverlayPosition.AnchorSection.BOTTOM_RIGHT)
   @Hud.Align(vertical = VerticalAlignment.BOTTOM, horizontal = HorizontalAlignment.RIGHT)
   private static class BombBell extends Hud.Element {
      @Override
      protected void onRender(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(ACTIVE_BOMBS, x, y);
      }

      @Override
      protected void onRenderPreview(float x, float y, float width, float height, PoseStack poseStack, float partialTicks, Window window) {
         render(List.of(
                 Example("FearsomeDuck81", BombType.LOOT, "WC36"),
                 Example("jhil", BombType.PROFESSION_XP, "WC3"),
                 Example("Essentuan", BombType.COMBAT_XP, "WC23"),
                 Example("CudlessTheBear", BombType.PROFESSION_SPEED, "WC70")
         ), x, y);
      }

      private static BombInfo Example(String user, BombType bombType, String server) {
         return new BombInfo(user, bombType, server, System.currentTimeMillis() + 500, bombType.getActiveMinutes());
      }

      private void render(List<BombInfo> bombs, float x, float y) {
         if (bombs.isEmpty()) return;

         new TextBox(builder -> builder.append(bombs, bomb -> {
            if (display_bomb_thrower) {
               builder.append(bomb.user(), ChatFormatting.YELLOW)
                       .append('\'');

               if (bomb.user().charAt(bomb.user().length() - 1) != 's') builder.append("s");

               builder.space();
            }

            builder.append(bomb.bomb().getName())
                    .append(" Bomb on ")
                    .append(bomb.server())
                    .append(" - ")
                    .append(bomb.getRemainingString());
         }), x, y).setTextColor(text_color)
                 .setTextStyle(style)
                 .setFill(background_color)
                 .with(this)
                 .setPadding(5, 5, 5, 5)
                 .setMaxWidth(getWidth())
                 .dynamic()
                 .build();
      }
   }

   @SubscribeEvent
   private static void onTick(TickEvent event) {
      ACTIVE_BOMBS = Bomb.getBombBells().stream()
              .sorted(COMPARATOR).toList();
   }

   private static final Comparator<BombInfo> COMPARATOR = Comparing.of(BombInfo::startTime, info -> info.bomb().getName(), BombInfo::user);
}
