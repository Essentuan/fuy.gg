package com.busted_moments.client.features.lootrun;

import com.busted_moments.client.events.mc.entity.EntityEvent;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Feature;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.util.CharUtil;
import com.wynntils.core.components.Managers;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.PlayerInteractEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.gear.type.GearTier;
import com.wynntils.models.worlds.event.WorldStateEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.joml.Vector2d;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynntils.utils.mc.McUtils.player;

@Config.Category("Lootruns")
@Feature.Definition(name = "Lootrun Dry Streak")
public class LootrunDryStreakFeature extends Feature {
   private static final Pattern PATTERN = Pattern.compile("You have (?<pulls>\\d*) rewards to pull");
   private static final StyledText MYTHIC_ITEM = TextBuilder.of("Mythic Item", ChatFormatting.DARK_PURPLE).build();

   @Hidden("Mythics")
   private static List<Pull> mythics = new ArrayList<>();

   @Hidden("Pulls")
   private static int pullsSinceLastMythic = 0;

   private static Vector2d pos;
   private static int pulls = 0;
   private static boolean readyForPull = false;
   private static boolean hasStarted = false;

   @Instance
   private static LootrunDryStreakFeature INSTANCE;

   private static final Set<UUID> items = new HashSet<>();

   @SubscribeEvent
   private static void onEntitySpawn(EntityEvent.SetData event) {
      Entity entity = event.getEntity();

      Matcher matcher;
      if (
              entity.getType() == EntityType.ARMOR_STAND &&
                      (matcher = StyledText.fromComponent(entity.getDisplayName()).getMatcher(PATTERN, PartStyle.StyleType.NONE)).matches()
      ) {
         pos = pos(entity);
         pulls = Integer.parseInt(matcher.group("pulls"));
      } else if (pos != null && entity instanceof ItemEntity stack && isClose(entity)) {
         if (items.contains(stack.getUUID()))
            return;

         if (readyForPull) {
            readyForPull = false;
            pullsSinceLastMythic += pulls;
         }
         items.add(stack.getUUID());

         if (GearTier.fromComponent(stack.getItem().getHoverName()) == GearTier.MYTHIC)
            push(stack.getItem());

         Managers.TickScheduler.scheduleLater(() -> {
            readyForPull = true;

            items.clear();
         }, 5);
      }
   }

   private static void push(ItemStack stack) {
      Pull pull = new Pull(stack, pullsSinceLastMythic);

      mythics.add(pull);
      pullsSinceLastMythic = 0;

      String name = ChatUtil.strip(stack.getHoverName().getString());

      if (INSTANCE.isEnabled())
         ChatUtil.message(TextBuilder.of("You have found a", ChatFormatting.LIGHT_PURPLE)
                 .appendIf(!name.isEmpty() && CharUtil.isVowel(name.charAt(0)), "n")
                 .space()
                 .append(stack)
                 .underline()
                 .append(" after ", ChatFormatting.LIGHT_PURPLE)
                 .underline(false)
                 .append(pull.pulls(), ChatFormatting.GOLD)
                 .append(" pulls!", ChatFormatting.LIGHT_PURPLE));
   }

   @SubscribeEvent
   private static void onTick(TickEvent event) {
      if (pos == null || !Models.WorldState.onWorld() || !hasStarted || pos.distance(pos(player())) < 15)
         return;

      reset();
   }

   @SubscribeEvent
   private static void onEntityInteract(PlayerInteractEvent.InteractAt event) {
      if (event.getTarget().getType() != EntityType.SLIME || !isClose(event.getTarget()))
         return;

      hasStarted = true;
      readyForPull = true;
   }

   @SubscribeEvent
   private static void onWorldState(WorldStateEvent event) {
      reset();
   }

   private static final double MAX_DISTANCE = 0.5;

   private static void reset() {
      if (pos != null && (INSTANCE.isEnabled() && pullsSinceLastMythic != 0))
            ChatUtil.message(TextBuilder.of("You've gone ", ChatFormatting.LIGHT_PURPLE)
                    .append(pullsSinceLastMythic, ChatFormatting.GOLD)
                    .append(" pulls without finding a ", ChatFormatting.LIGHT_PURPLE)
                    .append("Mythic", ChatFormatting.DARK_PURPLE)
                    .append(".", ChatFormatting.LIGHT_PURPLE));

      pos = null;
      hasStarted = false;
      readyForPull = false;
      pulls = 0;
   }

   private static boolean isClose(Entity entity) {
      if (pos == null)
         return false;

      return pos.distance(pos(entity)) < MAX_DISTANCE;
   }

   private static Vector2d pos(Entity entity) {
      return new Vector2d(entity.getX(), entity.getZ());
   }

   public static class Pull extends BaseModel {
      @Key
      private ItemStack item;
      @Key
      private int pulls;

      public Pull() {

      }

      private Pull(ItemStack stack, int pulls) {
         this.item = stack;
         this.pulls = pulls;
      }

      public ItemStack item() {
         return item;
      }

      public int pulls() {
         return pulls;
      }
   }

   public static List<Pull> pulls() {
      return List.copyOf(mythics);
   }

   public static int dry() {
      return pullsSinceLastMythic;
   }
}
