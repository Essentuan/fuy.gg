package com.busted_moments.client.models.war;

import com.busted_moments.client.models.territory.TerritoryModel;
import com.busted_moments.client.models.territory.events.TerritoryCapturedEvent;
import com.busted_moments.client.models.war.events.*;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Model;
import com.busted_moments.core.http.requests.mapstate.Territory;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.BossHealthUpdateEvent;
import com.wynntils.mc.event.ScoreboardSetDisplayObjectiveEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import com.wynntils.utils.mc.McUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.world.BossEvent;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WarModel extends Model implements ClientboundBossEventPacket.Handler {
   private static final Pattern TERRITORY_CAPTURE_REGEX = Pattern.compile("^\\[WAR\\] You have taken control of (?<territory>.+) from \\[(?<guild>.+)\\]!.*");
   private static final Pattern DEATH_REGEX = Pattern.compile("^You have died...");

   private War current;
   private boolean HAS_ENDED = false;

   private Territory LAST_TERRITORY;

   @Instance
   private static WarModel THIS;

   @SubscribeEvent(receiveCanceled = true)
   public void onBossbarUpdate(BossHealthUpdateEvent event) {
      event.getPacket().dispatch(this);
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onTick(TickEvent event) {
      TerritoryModel.getCurrentTerritory().ifPresent(territory -> LAST_TERRITORY = territory);
   }

   @SubscribeEvent(receiveCanceled = true)
   @SuppressWarnings("DataFlowIssue")
   public void onSetObjectiveDisplay(ScoreboardSetDisplayObjectiveEvent event) {
      Scoreboard scoreboard = McUtils.mc().level.getScoreboard();
      Objective objective = scoreboard.getObjective(event.getObjectiveName());

      scoreboard.getPlayerScores(objective)
              .stream()
              .filter(score -> ChatUtil.strip(score.getOwner()).contains("War:"))
              .findFirst().ifPresentOrElse(score -> {
                 if (current == null) {
                    current = new War(LAST_TERRITORY, new Date());
                    new WarEnterEvent(current).post();
                 }
              }, () -> {
                 current = null;
                 HAS_ENDED = false;
              });
   }

   @SubscribeEvent
   public void onMessage(ChatMessageReceivedEvent event) {
      if (current == null || HAS_ENDED) return;

      Matcher matcher = event.getOriginalStyledText().getMatcher(TERRITORY_CAPTURE_REGEX, PartStyle.StyleType.NONE);
      if (matcher.matches() && current.getTerritory().getName().equals(matcher.group("territory"))) {
         Tower.Stats previous = current.getTower().getStats();
         if (previous.health() != 0)
            onTowerUpdate(new Tower.Stats(0, previous.defense(), previous.damageMax(), previous.damageMin(), previous.attackSpeed()));

         new WarCompleteEvent(current).post();
         current.end();
         HAS_ENDED = true;
      } else if (event.getOriginalStyledText().matches(DEATH_REGEX, PartStyle.StyleType.NONE))
         new WarLeaveEvent(current, WarLeaveEvent.Cause.DEATH).post();
   }

   @SubscribeEvent
   public void onWorldState(WorldStateEvent event) {
      if (current != null && current.getTower() == null || HAS_ENDED) return;

      if (event.getNewState() != WorldState.WORLD && current != null)
         new WarLeaveEvent(current, WarLeaveEvent.Cause.HUB).post();
   }

   @SubscribeEvent
   public void onTerritoryCapture(TerritoryCapturedEvent event) {
      if (current != null && !HAS_ENDED && event.getTerritory().equals(current.getTerritory().getName()))
         new WarLeaveEvent(current, WarLeaveEvent.Cause.CAPTURED).post();
   }


   private void onTowerUpdate(Tower.Stats stats) {
      if (current == null || HAS_ENDED) return;

      if (current.tower == null) {
         current.startedAt = new Date();
         current.tower = new Tower(stats);

         new WarStartEvent(current).post();
         new TowerDamagedEvent(current, current.tower.UPDATES.get(current.tower.size() - 1)).post();

         return;
      }

      current.tower.add(stats);
      new TowerDamagedEvent(current, current.tower.UPDATES.get(current.tower.size() - 1)).post();
   }

   @Override
   public void add(@NotNull UUID id,
                   @NotNull Component name,
                   float progress,
                   @NotNull BossEvent.BossBarColor color,
                   @NotNull BossEvent.BossBarOverlay overlay,
                   boolean darkenScreen,
                   boolean playMusic,
                   boolean createWorldFog
   ) {
      Tower.Stats.from(StyledText.fromComponent(name)).ifPresent(this::onTowerUpdate);
   }

   @Override
   public void updateName(@NotNull UUID id, @NotNull Component name) {
      Tower.Stats.from(StyledText.fromComponent(name)).ifPresent(this::onTowerUpdate);
   }

   public static Optional<War> current() {
      if (THIS == null) return Optional.empty();

      return Optional.ofNullable(THIS.current);
   }

   public static WarModel getInstance() {
      return THIS;
   }
}