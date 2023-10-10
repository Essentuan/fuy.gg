package com.busted_moments.client.features.war;

import com.busted_moments.client.events.mc.entity.EntityEvent;
import com.busted_moments.client.util.EntityUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.util.NumUtil;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.ConnectionEvent;
import com.wynntils.models.players.profile.GuildProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraft.ChatFormatting.*;

@Default(State.ENABLED)
@Config.Category("War")
@Feature.Definition(name = "Season Leaderboard Helper")
public class SeasonLeaderboardFeature extends Feature {
   private int season = -1;
   private final List<Entry> leaderboard = new ArrayList<>(100);
   private final Set<Entity> entities = new HashSet<>();

   private static final Pattern LEADERBOARD_ENTRY_PATTERN = Pattern.compile("(?<rank>.*) - (?<guild>[^\\[]*) (.*)?\\((?<sr>.*) SR\\)");
   private static final Pattern LEADERBOARD_SEASON_PATTERN = Pattern.compile("Season (?<season>.*) Leaderboard");

   @SubscribeEvent
   public void onEntitySpawn(EntityEvent.SetData event) {
      if (event.getType() != EntityType.ARMOR_STAND) return;
      if (!EntityUtil.isNear(event.getEntity(),
              new Vec3(-303, 47, -4979),
              10
      )) return;

      StyledText text = StyledText.fromComponent(event.getEntity().getDisplayName());
      Matcher matcher;

      if ((matcher = text.getMatcher(LEADERBOARD_ENTRY_PATTERN, PartStyle.StyleType.NONE)).matches()) {
         entities.add(event.getEntity());
         if (season != -1) process(matcher);
      } else if ((matcher = text.getMatcher(LEADERBOARD_SEASON_PATTERN, PartStyle.StyleType.NONE)).matches()) {
         int s = parseInt(matcher, "season");

         if (s != season) {
            season = s;
            leaderboard.clear();

            for (Entity entity : entities) {
               text = StyledText.fromComponent(entity.getDisplayName());
               matcher = text.getMatcher(LEADERBOARD_ENTRY_PATTERN, PartStyle.StyleType.NONE);

               process(matcher);
            }

            update();
         }
      }
   }

   @SubscribeEvent
   public void onEntityRemove(EntityEvent.Remove event) {
      entities.remove(event.getEntity());

      if (entities.isEmpty() & !leaderboard.isEmpty()) leaderboard.clear();
   }

   @SubscribeEvent
   public void onDisconnect(ConnectionEvent.DisconnectedEvent event) {
      entities.clear();
      leaderboard.clear();
   }

   private void process(Matcher matcher) {
      if (!matcher.matches()) return;

      Entry entry = Entry.of(matcher);

      if (leaderboard.size() < entry.rank()) {
         int toAdd = ((int) Math.ceil(entry.rank() / 10D) * 10) - leaderboard.size();
         for (int i = 0; i < toAdd; i++)
            leaderboard.add(null);
      };

      leaderboard.set(entry.rank() - 1, entry);

      update();
   }

   private ChatFormatting[] color(int rank) {
      if (rank == 1) {
         return new ChatFormatting[]{GOLD, BOLD};
      }
      if (rank <= 3)
         return new ChatFormatting[]{GOLD};
      else if (rank <= 6)
         return new ChatFormatting[]{YELLOW};
      else if (rank <= 9)
         return new ChatFormatting[]{WHITE};
      else
         return new ChatFormatting[]{GRAY};
   }

   private void update() {
      entities.forEach(entity -> {
         StyledText text = StyledText.fromComponent(entity.getDisplayName());
         Matcher matcher = text.getMatcher(LEADERBOARD_ENTRY_PATTERN, PartStyle.StyleType.NONE);
         if (!matcher.matches()) return;

         int rank = parseInt(matcher, "rank");

         var res = leaderboard.get(rank - 1);
         var entry = res == null ? Entry.of(matcher) : res;

         var behind = rank < leaderboard.size() ? leaderboard.get(rank) : null;

         entity.setCustomName(
                 TextBuilder.empty()
                         .append(rank, color(rank))
                         .append(" - ", GRAY)
                         .append(entry.guild(), AQUA)
                         .appendIf(entry::hasPrefix, " [%s]", entry.prefix(), AQUA)
                         .append(" (", LIGHT_PURPLE)
                         .append(NumUtil.format(entry.sr()), LIGHT_PURPLE)
                         .append(" SR)", LIGHT_PURPLE)
                         .appendIf(
                                 behind != null,
                                 " (+%s)",
                                 () -> new Object[]{NumUtil.format(entry.sr() - behind.sr())},
                                 GREEN
                         ).toComponent());
      });
   }

   private static int parseInt(Matcher matcher, String group) {
      return NumUtil.parseInt(matcher.group(group).replaceAll(" ", ""));
   }

   private record Entry(int rank, String guild, @Nullable String prefix, int sr) {
      private Entry(int rank, String guild, int sr) {
         this(
                 rank,
                 guild,
                 Models.Guild.getGuildProfile(guild).map(GuildProfile::prefix).orElse(null),
                 sr
         );
      }

      public boolean hasPrefix() {
         return prefix != null;
      }

      public static Entry of(Matcher matcher) {
         return new Entry(
                 parseInt(matcher, "rank"),
                 matcher.group("guild"),
                 parseInt(matcher, "sr")
         );
      }
   }
}
