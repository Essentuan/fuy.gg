package com.busted_moments.client.models.territory;

import com.busted_moments.client.events.mc.MinecraftStartupEvent;
import com.busted_moments.client.models.territory.events.MapUpdateEvent;
import com.busted_moments.client.models.territory.events.TerritoryCapturedEvent;
import com.busted_moments.core.Model;
import com.busted_moments.core.http.requests.mapstate.MapState;
import com.busted_moments.core.http.requests.mapstate.Territory;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.time.ChronoUnit;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.wynntils.utils.mc.McUtils.player;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class TerritoryModel extends Model {
   private final static Pattern TERRITORY_CAPTURED_PATTERN = Pattern.compile("^\\[WAR] \\[(?<guild>.+)] captured the territory (?<territory>.+)\\.");
   private final static Pattern TERRITORY_CONTROL_PATTERN = Pattern.compile("^\\[INFO] \\[(?<guild>.+)] has taken control of (?<territory>.+)!");

   @SuppressWarnings("unused")
   private Socket ACTIVE_SOCKET;

   private MapState LATEST_TERRITORIES;

   @Instance
   private static TerritoryModel THIS;

   private static Optional<Territory> CURRENT_TERRITORY = Optional.empty();

   @SubscribeEvent
   public void onMessage(ChatMessageReceivedEvent event) {
      Matcher matcher = event.getOriginalStyledText().getMatcher(TERRITORY_CAPTURED_PATTERN, PartStyle.StyleType.NONE);
      if (matcher.matches()) {
         new TerritoryCapturedEvent(matcher.group("territory"), matcher.group("guild")).post();
         return;
      }

      matcher = event.getOriginalStyledText().getMatcher(TERRITORY_CONTROL_PATTERN, PartStyle.StyleType.NONE);
      if (matcher.matches())
         new TerritoryCapturedEvent(matcher.group("territory"), matcher.group("guild")).post();
   }


   @SubscribeEvent
   public static void onGameStart(MinecraftStartupEvent event) {
      new Territory.Request().thenAccept(optional -> {
         new MapUpdateEvent(optional.orElse(MapState.empty())).post();

         THIS.ACTIVE_SOCKET = THIS.new Socket();
      });
   }

   @SubscribeEvent(priority = EventPriority.LOWEST)
   public void onMapUpdate(MapUpdateEvent event) {
      if (LATEST_TERRITORIES != null)
         event.getState().forEach(territory -> {
            if (LATEST_TERRITORIES.contains(territory) && !LATEST_TERRITORIES.get(territory).getOwner().equals(territory.getOwner()))
               new TerritoryCapturedEvent(territory.getName(), territory.getOwner().prefix());
         });

      LATEST_TERRITORIES = event.getState();
   }

   @SubscribeEvent
   public void onTick(TickEvent event) {
      if (LATEST_TERRITORIES != null) {
         CURRENT_TERRITORY = LATEST_TERRITORIES.stream()
                 .filter(territory -> territory.getLocation().isInside(player().position()))
                 .findFirst();
      }
   }

   public static MapState getTerritoryList() {
      return THIS.LATEST_TERRITORIES;
   }

   public static Optional<Territory> getCurrentTerritory() {
      return CURRENT_TERRITORY;
   }

   protected class Socket extends WebSocketClient {
      private static final URI SOCKET_URI = URI.create("wss://thesimpleones.net/war");

      public Socket() {
         super(SOCKET_URI);

         connect();
      }

      @Override
      public void onOpen(ServerHandshake handshake) {

      }

      @Override
      public void onMessage(String message) {
         Json.tryParse(message).ifPresent(json -> {
            if (json.getString("type").equals("MapUpdate")) {
               new MapUpdateEvent(json.getJson("map").wrap(MapState::new)).post();
            }
         });
      }

      @Override
      public void onClose(int code, String reason, boolean remote) {
         LOGGER.warn("Territory socket has disconnected with code {} (reason={}, remote={})", code, reason, reason);

         Heartbeat.schedule(() -> ACTIVE_SOCKET = new Socket(), 10, ChronoUnit.SECONDS);
      }

      @Override
      public void onError(Exception ex) {
         LOGGER.error("Error in socket", ex);
      }
   }
}
