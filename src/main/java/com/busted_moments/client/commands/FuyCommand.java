package com.busted_moments.client.commands;

import com.busted_moments.client.features.AutoStreamFeature;
import com.busted_moments.client.features.ExtendedGuildOnlineMembersFeature;
import com.busted_moments.client.features.AutoUpdateFeature;
import com.busted_moments.client.features.lootrun.LootrunDryStreakFeature;
import com.busted_moments.client.features.war.WeeklyWarCountOverlay;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.http.api.Find;
import com.busted_moments.core.http.api.guild.Guild;
import com.busted_moments.core.http.api.guild.GuildType;
import com.busted_moments.core.http.api.player.Player;
import com.busted_moments.core.http.requests.serverlist.ServerList;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.tuples.Pair;
import com.busted_moments.core.util.StringUtil;
import com.busted_moments.core.util.iterators.Iter;
import com.essentuan.acf.core.annotations.Alias;
import com.essentuan.acf.core.annotations.Argument;
import com.essentuan.acf.core.annotations.Command;
import com.essentuan.acf.core.annotations.Inherit;
import com.essentuan.acf.core.annotations.Subcommand;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.String.StringType;
import com.essentuan.acf.fabric.core.client.FabricClientCommandSource;
import com.mojang.brigadier.context.CommandContext;
import com.wynntils.core.components.Managers;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Objects;
import java.util.function.Consumer;

import static com.busted_moments.client.Client.CONFIG;
import static com.busted_moments.client.util.ChatUtil.prefixLength;
import static com.busted_moments.core.time.ChronoUnit.MINUTES;
import static com.busted_moments.core.time.ChronoUnit.SECONDS;
import static com.busted_moments.core.time.FormatFlag.COMPACT;
import static com.mojang.brigadier.arguments.StringArgumentType.StringType.GREEDY_PHRASE;
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.DARK_AQUA;
import static net.minecraft.ChatFormatting.DARK_PURPLE;
import static net.minecraft.ChatFormatting.GRAY;
import static net.minecraft.ChatFormatting.LIGHT_PURPLE;
import static net.minecraft.ChatFormatting.RED;
import static net.minecraft.ChatFormatting.WHITE;
import static net.minecraft.ChatFormatting.YELLOW;

@Alias("fy")
@Command("fuy")
@Inherit({RaidCommand.class})
public class FuyCommand {
   @Subcommand("config")
   private static void onConfig(CommandContext<FabricClientCommandSource> context) {
      CONFIG.open();
   }

   @Alias("as")
   @Subcommand("autostream")
   private static void onAutoStream(
           CommandContext<FabricClientCommandSource> context
   ) {
      AutoStreamFeature.get().toggle();
   }

   @Alias("om")
   @Subcommand("onlinemembers")
   private static void getOnlineMembers(
           CommandContext<FabricClientCommandSource> context,
           @Argument("Guild") @StringType(GREEDY_PHRASE) String string
   ) {
      ChatUtil.message("Finding guild %s...".formatted(string), ChatFormatting.GREEN);
	  ExtendedGuildOnlineMembersFeature OMFeature = ExtendedGuildOnlineMembersFeature.get();
      boolean extended = OMFeature.isEnabled();
      Date start = new Date();
      new Guild.Request(string).thenAccept(optional -> optional.ifPresentOrElse(guild -> {
         List<Guild.Member> online = guild.stream().filter(member -> member.world().isPresent()).toList();

         ChatUtil.message(TextBuilder.of(guild.name(), AQUA).space()
                 .append("[", DARK_AQUA).append(guild.prefix(), AQUA).append("]", DARK_AQUA).space()
                 .append("has ", GRAY)
                 .append(online.size(), AQUA)
                 .append(" of ", GRAY)
                 .append(guild.size(), AQUA)
                 .append(" members online: ", GRAY)
                 .append(online, (member, b) -> b
                                 .append(StringUtil.nCopies("\u2605", member.rank().countStars()) + member.username(), AQUA)
                                 .onPartHover(builder -> builder
                                         .append("Click to switch to ", GRAY)
                                         .append(member.world().orElseThrow(), WHITE)
                                         .line()
                                         .append("(Requires ", DARK_PURPLE)
                                         .append("HERO", LIGHT_PURPLE)
                                         .append(" rank)", DARK_PURPLE))
                                 .onPartClick(ClickEvent.Action.RUN_COMMAND, "/switch " + member.world().orElseThrow())
								 .append(extended ? " (" + member.world().orElseThrow() + ")": "", (Objects.equals(OMFeature.getLastKnownServer(member.username()),member.world().orElseThrow())) ? GRAY : YELLOW)
                                 .onPartHover(builder -> builder
                                         .append("Player has moved ", GRAY)
										 .append( (OMFeature.getLastKnownServer(member.username()) == null) ? "None" : OMFeature.getLastKnownServer(member.username()), WHITE)
										 .append(" -> ", GRAY)
                                         .append(OMFeature.setLastKnownServer(member.username(),member.world().orElseThrow()), WHITE)
										 .line()
										 .append("Click to switch to ", GRAY)
                                         .append(member.world().orElseThrow(), WHITE)
                                         .line()
                                         .append("(Requires ", DARK_PURPLE)
                                         .append("HERO", LIGHT_PURPLE)
                                         .append(" rank)", DARK_PURPLE))
                                 .onPartClick(ClickEvent.Action.RUN_COMMAND, "/switch " + member.world().orElseThrow()),
                         b -> b.append(", ", AQUA)
                 ));
      }, () -> {
         if (Duration.since(start).greaterThan(10, ChronoUnit.SECONDS)) {
            ChatUtil.message("Timeout finding guild %s".formatted(string), RED);
         } else ChatUtil.message("Could not find guild %s".formatted(string), RED);
      }));
   }

   @Subcommand("find")
   private static void onFindPlayer(
           CommandContext<FabricClientCommandSource> context,
           @Argument("Player") String string
   ) {
      ChatUtil.message("Finding player %s...".formatted(string), ChatFormatting.GREEN);

      new Find.Request(string).thenAccept(optional -> optional.ifPresentOrElse(player -> player.getWorld().ifPresentOrElse(world -> ChatUtil.message(TextBuilder.of(player.getUsername(), AQUA)
                      .append(" is on ", GRAY)
                      .append(world, AQUA)), () -> ChatUtil.message(TextBuilder.of(player.getUsername(), AQUA)
                      .append(" is not ", GRAY).append("online", AQUA))),
              () -> ChatUtil.message("Could not find player %s".formatted(string), ChatFormatting.RED)));
   }

   @Alias("ls")
   @Subcommand("lastseen")
   private static void getLastSeen(
           CommandContext<FabricClientCommandSource> context,
           @Argument("Player") String string
   ) {
      getPlayer(string, player -> {
         Duration lastSeen = Duration.since(player.lastJoin());

         player.world().ifPresentOrElse(world ->
                 ChatUtil.message(TextBuilder.of(player.username(), AQUA)
                         .append(" is on ", GRAY)
                         .append(world)), () -> ChatUtil.message(TextBuilder.of(player.username(), AQUA)
                 .append(" was last seen ", GRAY)
                 .append(lastSeen.getPart(MINUTES) > 1 ? lastSeen.toString(COMPACT, MINUTES) : lastSeen.toString(COMPACT, ChronoUnit.SECONDS), AQUA)
                 .append(" ago", GRAY)));
      });
   }

   @Alias("sp")
   @Subcommand("soulpoints")
   private static void getSoulPointWorlds(
           CommandContext<FabricClientCommandSource> context
   ) {
      new ServerList.Request().thenApply(Optional::orElseThrow).thenAccept(servers -> {
         TextBuilder builder = TextBuilder.of("Worlds near Sunrise: ", GRAY).line();

         builder.append(Iter.of(
                 servers.stream()
                         .filter(world -> !world.getWorld().isBlank())
                         .map(world -> Pair.of(
                                 world,
                                 Duration.of(20, MINUTES).minus(world.getUptime().mod(20, MINUTES)).minus(90, SECONDS)
                         )).sorted(Comparator.comparing(Pair::two))
                         .limit(10)
                         .iterator()
         ), pair -> builder.append(pair.one().getWorld(), AQUA)
                 .append(" is ", GRAY)
                 .append(pair.two().toString(COMPACT, SECONDS), AQUA)
                 .append(" away from Sunrise", GRAY)
                 .onHover(b -> b
                         .append("Click to switch to ", GRAY)
                         .append(pair.one().getWorld(), WHITE)
                         .line()
                         .append("(Requires ", DARK_PURPLE)
                         .append("HERO", LIGHT_PURPLE)
                         .append(" rank)", DARK_PURPLE))
                 .onClick(ClickEvent.Action.RUN_COMMAND, "/switch " + pair.one().getWorld()));

         Managers.TickScheduler.scheduleNextTick(() -> ChatUtil.message(builder));
      });
   }

   @Alias("pg")
   @Subcommand("playerguild")
   private static void getPlayerGuild(
           CommandContext<FabricClientCommandSource> context,
           @Argument("Player") String string
   ) {
      getPlayer(string, player -> {
         if (player.guild().isEmpty()) {
            ChatUtil.message(TextBuilder.of(player.username(), AQUA).append(" is not in a ", GRAY).append("guild", AQUA));
         } else {
            player.guild().map(GuildType::name).map(Guild.Request::new).orElseThrow().thenAccept(optional -> {
               Player.Guild playerGuild = player.guild().orElseThrow();

               TextBuilder builder = TextBuilder.of(player.username(), AQUA)
                       .append(" is a ", GRAY).append(playerGuild.rank().prettyPrint(), AQUA)
                       .append(" in ", GRAY)
                       .append(playerGuild.prettyPrint(), AQUA);

               optional.ifPresent(guild -> {
                  if (guild.contains(player)) {
                     Guild.Member member = guild.get(player);
                     Duration duration = Duration.since(member.joinedAt());

                     builder.append(". They have been in the guild for ", GRAY)
                             .append(duration.getPart(MINUTES) > 1 ? duration.toString(COMPACT, MINUTES) : duration.toString(COMPACT, ChronoUnit.SECONDS), AQUA)
                             .append(".", GRAY);
                  }
               });

               ChatUtil.message(builder);
            });
         }
      });
   }

   @Subcommand("wars")
   private static void getWars(
           CommandContext<FabricClientCommandSource> context,
           @Argument("Since") @StringType(GREEDY_PHRASE) Duration range
   ) {
      long wars = WeeklyWarCountOverlay.getWars()
              .stream()
              .filter(war -> Duration.since(war).lessThanOrEqual(range))
              .count();

      ChatUtil.message(
              TextBuilder.of("You have entered ", GRAY)
                      .append(wars, AQUA)
                      .append(" war", GRAY).appendIf(() -> wars != 1, "s", GRAY)
                      .append(" in the past ")
                      .append(range.toString(), AQUA).append(".", GRAY)
      );
   }

   @Subcommand("update")
   private static void onUpdate(CommandContext<?> context) {
      ChatUtil.message("Attempting update...", YELLOW);

      AutoUpdateFeature.update().thenAccept(result -> ChatUtil.message(result.getMessage()));
   }

   private static void getPlayer(String string, Consumer<Player> consumer) {
      ChatUtil.message("Finding player %s...".formatted(string), ChatFormatting.GREEN);

      new Player.Request(string).thenAccept(optional -> optional.ifPresentOrElse(consumer, () -> ChatUtil.message("Could not find player %s".formatted(string), ChatFormatting.RED)));
   }

   private static final int ITEMS_PER_PAGE = 5;

   @Subcommand("drystreak")
   private static void onGetDryStreak(CommandContext<?> context) {
      ChatUtil.message(TextBuilder.of("You've gone ", ChatFormatting.LIGHT_PURPLE)
              .append(LootrunDryStreakFeature.dry(), ChatFormatting.GOLD)
              .append(" pulls without finding a ", ChatFormatting.LIGHT_PURPLE)
              .append("Mythic", ChatFormatting.DARK_PURPLE)
              .append(".", ChatFormatting.LIGHT_PURPLE));
   }

   @Subcommand("drystreak average")
   private static void onGetDryStreakAverage(CommandContext<?> context) {
      ChatUtil.message(
              TextBuilder.of("You average ", ChatFormatting.LIGHT_PURPLE)
                      .append(
                              (int) LootrunDryStreakFeature.pulls()
                                      .stream()
                                      .mapToInt(LootrunDryStreakFeature.Pull::pulls)
                                      .average()
                                      .orElse(0),
                              ChatFormatting.GOLD
                      )
                      .append(" pulls between ", ChatFormatting.LIGHT_PURPLE)
                      .append("Mythics", ChatFormatting.DARK_PURPLE)
                      .append(".", ChatFormatting.LIGHT_PURPLE)
      );
   }

   @Subcommand("drystreak history")
   private static void getDryStreakHistory(CommandContext<?> context) {
      getDryStreakPage(context, 0);
   }

   @Subcommand("drystreak history page")
   private static void getDryStreakPage(
           CommandContext<?> context,
           @Argument("Page") int page
   ) {
      List<LootrunDryStreakFeature.Pull> pulls = LootrunDryStreakFeature.pulls();

      TextBuilder builder = TextBuilder.of("\n\n");

      if (pulls.isEmpty())
         builder.append("There is nothing to display", ChatFormatting.WHITE)
                 .underline()
                 .center(prefixLength())
                 .line();
      else
         pulls.stream()
                 .skip(page * ITEMS_PER_PAGE)
                 .limit(ITEMS_PER_PAGE)
                 .forEach(pull -> builder.append("")
                         .reset()
                         .append(pull.item())
                         .underline()
                         .center(prefixLength())
                         .line()
                         .line()
                         .append("After ", ChatFormatting.LIGHT_PURPLE)
                         .append(pull.pulls(), ChatFormatting.GOLD)
                         .append(" pulls.", ChatFormatting.LIGHT_PURPLE)
                         .center(prefixLength())
                         .line().line());

      int maxPages = (int) Math.ceil(pulls.size() / (double) ITEMS_PER_PAGE);

      boolean hasNext = page < maxPages - 1;
      boolean hasPrevious = page > 0;

      builder.append("⋘",
              hasPrevious ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY);

      if (hasPrevious)
         builder
                 .onPartClick(ClickEvent.Action.RUN_COMMAND, "/fuy drystreak history page " + (page - 1));
      else
         builder.strikethrough();

      builder
              .append("   ")
              .reset()
              .append(page + 1, ChatFormatting.WHITE)
              .append("/", ChatFormatting.GRAY)
              .append(maxPages, ChatFormatting.WHITE)
              .append("   ")
              .append("⋙", hasNext ? ChatFormatting.WHITE : ChatFormatting.DARK_GRAY);

      if (hasNext)
         builder
                 .onPartClick(ClickEvent.Action.RUN_COMMAND, "/fuy drystreak history page " + (page + 1));
      else
         builder.strikethrough();

      builder.center(prefixLength());

      ChatUtil.message(builder);
   }
}
