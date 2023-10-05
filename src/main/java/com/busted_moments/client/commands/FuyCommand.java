package com.busted_moments.client.commands;

import com.busted_moments.client.features.AutoStreamFeature;
import com.busted_moments.client.features.AutoUpdateFeature;
import com.busted_moments.client.features.raids.RaidsCommon;
import com.busted_moments.client.features.war.WeeklyWarCountOverlay;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.api.requests.Find;
import com.busted_moments.core.api.requests.Guild;
import com.busted_moments.core.api.requests.player.Player;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.FormatFlag;
import com.busted_moments.core.time.TimeUnit;
import com.essentuan.acf.core.annotations.Alias;
import com.essentuan.acf.core.annotations.Argument;
import com.essentuan.acf.core.annotations.Command;
import com.essentuan.acf.core.annotations.Subcommand;
import com.essentuan.acf.core.command.arguments.builtin.primitaves.String.StringType;
import com.essentuan.acf.fabric.core.client.FabricClientCommandSource;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;

import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import static com.busted_moments.client.FuyMain.CONFIG;
import static com.busted_moments.client.features.raids.NOGRaid.NOGPB;
import static com.busted_moments.client.features.raids.NOLRaid.NOLPB;
import static com.busted_moments.client.features.raids.RaidsCommon.timeCalc;
import static com.busted_moments.client.features.raids.TCCRaid.TCCPB;
import static com.busted_moments.client.features.raids.TNARaid.TNAPB;
import static com.mojang.brigadier.arguments.StringArgumentType.StringType.GREEDY_PHRASE;
import static net.minecraft.ChatFormatting.*;

@Alias("fy")
@Command("fuy")
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

      Date start = new Date();
      new Guild.Request(string).thenAccept(optional -> optional.ifPresentOrElse(guild -> {
         List<Guild.Member> online = guild.stream().filter(member -> member.getWorld().isPresent()).toList();

         ChatUtil.message(TextBuilder.of(guild.getName(), AQUA).space()
                 .append("[", DARK_AQUA).append(guild.getPrefix(), AQUA).append("]", DARK_AQUA).space()
                 .append("has ", GRAY)
                 .append(online.size(), AQUA)
                 .append(" of ", GRAY)
                 .append(guild.size(), AQUA)
                 .append(" members online: ", GRAY)
                 .append(online, (member, b) -> b
                                 .append(member.getRank().getStars() + member.getUsername())
                                 .onPartHover(builder -> builder
                                         .append("Click to switch to ", GRAY)
                                         .append(member.getWorld().orElseThrow(), WHITE)
                                         .line()
                                         .append("(Requires ", DARK_PURPLE)
                                         .append("HERO", LIGHT_PURPLE)
                                         .append(" rank)", DARK_PURPLE))
                                 .onPartClick(ClickEvent.Action.RUN_COMMAND, "/switch " + member.getWorld().orElseThrow()),
                         b -> b.append(", ", AQUA)
                 ));
      }, () -> {
         if (Duration.since(start).greaterThan(10, TimeUnit.SECONDS)) {
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
         Duration lastSeen = Duration.since(player.getLastSeen());

         player.getWorld().ifPresentOrElse(world ->
                 ChatUtil.message(TextBuilder.of(player.getUsername(), AQUA)
                         .append(" is on ", GRAY)
                         .append(world)), () -> ChatUtil.message(TextBuilder.of(player.getUsername(), AQUA)
                 .append(" was last seen ", GRAY)
                 .append(lastSeen.getPart(TimeUnit.MINUTES) > 1 ? lastSeen.toString(FormatFlag.COMPACT, TimeUnit.MINUTES) : lastSeen.toString(FormatFlag.COMPACT, TimeUnit.SECONDS), AQUA)
                 .append(" ago", GRAY)));
      });
   }

   @Alias("pg")
   @Subcommand("playerguild")
   private static void getPlayerGuild(
           CommandContext<FabricClientCommandSource> context,
           @Argument("Player") String string
   ) {
      getPlayer(string, player -> {
         if (player.getGuildName() == null) {
            ChatUtil.message(TextBuilder.of(player.getUsername(), AQUA).append(" is not in a ", GRAY).append("guild", AQUA));
         } else {
            player.getGuild().thenAccept(optional -> {
               TextBuilder builder = TextBuilder.of(player.getUsername(), AQUA)
                       .append(" is a ", GRAY).append(player.getGuildRank().prettyPrint(), AQUA)
                       .append(" in ", GRAY)
                       .append(player.getGuildName(), AQUA);

               optional.ifPresent(guild -> {
                  if (guild.contains(player)) {
                     Guild.Member member = guild.get(player);
                     Duration duration = Duration.since(member.getJoinedAt());

                     builder.append(". They have been in the guild for ", GRAY)
                             .append(duration.getPart(TimeUnit.MINUTES) > 1 ? duration.toString(FormatFlag.COMPACT, TimeUnit.MINUTES) : duration.toString(FormatFlag.COMPACT, TimeUnit.SECONDS), AQUA)
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

   @Alias("rs")
   @Subcommand("raidStop")
   private static void raidStop(
           CommandContext<FabricClientCommandSource> context
   ) {
      if (RaidsCommon.inRaid){
         RaidsCommon.inRaid = false;
         ChatUtil.message("inRaid set to false!");
      }else{
         ChatUtil.message("inRaid already on false!");
      }
   }

   @Alias("rpb")
   @Subcommand("raidPBs")
   private static void raidPBs(
           CommandContext<FabricClientCommandSource> context
   ){
      TextBuilder builder = TextBuilder.of("The Nameless Anomaly: ", LIGHT_PURPLE).next()
              .append(timeCalc(TNAPB.toSeconds()), AQUA)
              .line()
              .append("The Canyon Colossus: ", LIGHT_PURPLE).next()
              .append(timeCalc(TCCPB.toSeconds()), AQUA)
              .line()
              .append("Nest of The Grootslangs: ", LIGHT_PURPLE).next()
              .append(timeCalc(NOGPB.toSeconds()), AQUA)
              .line()
              .append("Nexus of Light: ", LIGHT_PURPLE).next()
              .append(timeCalc(NOLPB.toSeconds()), AQUA)
              .line();
      ChatUtil.message(builder);
   }

}
