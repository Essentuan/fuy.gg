package com.busted_moments.client.models.war.timer;

import com.busted_moments.client.models.war.Defense;
import com.busted_moments.client.models.territory.TerritoryModel;
import com.busted_moments.client.models.territory.events.TerritoryCapturedEvent;
import com.busted_moments.core.Model;
import com.busted_moments.core.api.requests.mapstate.Territory;
import com.busted_moments.core.heartbeat.annotations.Schedule;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;
import com.busted_moments.core.util.TempMap;
import com.busted_moments.core.util.TempSet;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import com.wynntils.mc.event.InventoryMouseClickedEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.models.territories.TerritoryAttackTimer;
import com.wynntils.utils.mc.McUtils;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimerModel extends Model {
   private final static Pattern ATTACK_PATTERN = Pattern.compile("^\\[WAR\\] The war for (?<territory>.+) will start in (?<timer>.+).$");
   private final static Pattern DEFENSE_PATTERN = Pattern.compile("^\\[.*\\] (?<territory>.+) defense is (?<defense>.+)?$");
   private static final Pattern ATTACK_SCREEN_TITLE = Pattern.compile("Attacking: (?<territory>.+)");

   private final Set<Timer> TIMERS = new HashSet<>();
   private final Map<String, Defense> KNOWN_DEFENSES = new TempMap<>(10, TimeUnit.SECONDS);


   private final Set<String> PERSONALLY_QUEUED = new TempSet<>(10, TimeUnit.SECONDS);


   @Instance
   private static TimerModel THIS;

   private void addTimer(String territory, Duration timeRemaining, boolean trust) {
      if (!trust && getTimer(territory, timeRemaining).isPresent()) return;

      territory = findBestMatch(territory);
      if (territory == null) return;

      Timer timer = new Timer(territory, timeRemaining);
      if (getTimer(timer.getTerritory(), timer.getRemaining()).isPresent()) return;

      if (KNOWN_DEFENSES.containsKey(timer.getTerritory())) {
         timer.defense = KNOWN_DEFENSES.get(timer.getTerritory());
         timer.confident = true;
      } else timer.defense = Defense.get(timer.getTerritory());

      if (PERSONALLY_QUEUED.contains(timer.getTerritory())) timer.personal = true;

      TIMERS.add(timer);
   }

   @SubscribeEvent
   public void onMessage(ChatMessageReceivedEvent event) {
      if (event.getRecipientType() != RecipientType.GUILD) return;

      onGuildMessage(event);

      Matcher matcher = event.getOriginalStyledText().getMatcher(ATTACK_PATTERN, PartStyle.StyleType.NONE);
      if (!matcher.matches()) return;

      Duration.parse(matcher.group("timer")).ifPresent(timer -> addTimer(matcher.group("territory"), timer, true));
   }

   private void onGuildMessage(ChatMessageReceivedEvent event) {
      Matcher matcher = event.getOriginalStyledText().getMatcher(DEFENSE_PATTERN, PartStyle.StyleType.NONE);
      if (!matcher.matches()) return;

      String territory = matcher.group("territory");
      Defense defense = Defense.from(matcher.group("defense"));

      KNOWN_DEFENSES.put(territory, defense);

      for (Timer timer : TIMERS) {
         if (timer.getTerritory().equals(territory) && !timer.confident && Duration.since(timer.getStart()).lessThan(100, TimeUnit.MILLISECONDS)) {
            timer.defense = defense;
            timer.confident = true;

            break;
         }
      }
   }

   @SubscribeEvent
   public void onTerritoryCapture(TerritoryCapturedEvent event) {
      TIMERS.removeIf(timer -> timer.getTerritory().equals(event.getTerritory()));
   }

   @SubscribeEvent
   public void onInventoryClick(InventoryMouseClickedEvent event) {
      if (event.getHoveredSlot() == null || McUtils.mc().screen == null) return;
      Matcher titleMatcher = ATTACK_SCREEN_TITLE.matcher(McUtils.mc().screen.getTitle().getString());
      if (!titleMatcher.matches()) return;

      PERSONALLY_QUEUED.add(titleMatcher.group("territory"));
   }

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   @SuppressWarnings("ResultOfMethodCallIgnored")
   public void onClear(TickEvent event) {
      synchronized (TIMERS) {
         TIMERS.removeIf(timer -> timer.getRemaining().lessThanOrEqual(100, TimeUnit.MILLISECONDS));
      }

      synchronized (KNOWN_DEFENSES) {
         KNOWN_DEFENSES.size();
      }
   }

   private Set<Integer> SCOREBOARD = new HashSet<>();

   @Schedule(rate = 500, unit = TimeUnit.MILLISECONDS)
   private void HandleScoreboard() {
      Set<Integer> previous = SCOREBOARD;
      SCOREBOARD = new HashSet<>();

      for (TerritoryAttackTimer timer : Models.GuildAttackTimer.getAttackTimers()) {
         SCOREBOARD.add(hash(timer)); if (previous.contains(hash(timer))) continue;
         addTimer(timer.territory(), Duration.of(timer.asSeconds(), TimeUnit.SECONDS), false);
      }
   }

   private static int hash(TerritoryAttackTimer timer) {
      return Objects.hash(
              timer.territory(),
              timer.asSeconds()
      );
   }

   private static @Nullable String findBestMatch(String name) {
      Territory.List<?> list = TerritoryModel.getTerritoryList();
      if (list.contains(name)) return name;

      for (Territory territory : list) {
         if (territory.getName().startsWith(name)) return territory.getName();
      }

      return null;
   }

   public static Set<Timer> getTimers() {
      return THIS.TIMERS;
   }

   public static Optional<Timer> getTimer(String territory, Duration timeRemaining) {
      for (Timer timer : getTimers()) {
         if (timer.getTerritory().startsWith(territory) && Math.abs(timer.getRemaining().subtract(timeRemaining).toSeconds()) < 10000) {
            return Optional.of(timer);
         }
      }

      return Optional.empty();
   }
}
