package com.busted_moments.core.api.requests.player;

import com.busted_moments.core.Promise;
import com.busted_moments.core.api.internal.GetRequest;
import com.busted_moments.core.api.internal.RateLimit;
import com.busted_moments.core.api.requests.Guild;
import com.busted_moments.core.api.requests.player.Class.Class;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.json.template.JsonTemplate;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.time.TimeUnit;

import java.util.*;

public class Player extends JsonTemplate {
   @Entry
   private String username;
   @Entry
   private UUID uuid;

   @Entry
   @Nullable
   private String world;

   @Entry
   private PlayerRank playerRank;
   @Entry
   private StoreRank storeRank;
   @Entry
   private Date firstSeen;
   @Entry
   private Date lastSeen;
   @Entry
   private long playtime;
   @Entry
   private boolean isVeteran;
   @Entry
   private Map<UUID, Class> classes;
   @Entry
   private GuildInfo guild;
   @Entry
   private long totalMobsKilled;
   @Entry
   private long totalCombatLevel;
   @Entry
   private long totalProfessionLevel;
   @Entry
   private long totalLevel;
   @Entry
   private long totalLogins;
   @Entry
   private long totalDeaths;
   @Entry
   private long totalDiscoveries;
   @Entry
   private long pvpKills;
   @Entry
   private long pvpDeaths;

   public String getUsername() {
      return username;
   }

   public UUID getUuid() {
      return uuid;
   }

   public PlayerRank getPlayerRank() {
      return playerRank;
   }

   public StoreRank getStoreRank() {
      return storeRank;
   }

   public Date getFirstSeen() {
      return firstSeen;
   }

   public Date getLastSeen() {
      return lastSeen;
   }

   public Duration getPlaytime() {
      return Duration.of(playtime, TimeUnit.MILLISECONDS);
   }

   public boolean isVeteran() {
      return isVeteran;
   }

   public List<Class> getClassList() {
      return new ArrayList<>(getClasses().values());
   }

   public Map<UUID, Class> getClasses() {
      return classes;
   }

   public Promise<Optional<Guild>> getGuild() {
      return guild.asGuild();
   }


   public String getGuildName() {
      return guild.getName();
   }

   public Guild.Rank getGuildRank() {
      return guild.getRank();
   }

   public long getTotalMobsKilled() {
      return totalMobsKilled;
   }

   public long getTotalCombatLevel() {
      return totalCombatLevel;
   }

   public long getTotalProfessionLevel() {
      return totalProfessionLevel;
   }

   public long getTotalLevel() {
      return totalLevel;
   }

   public long getTotalLogins() {
      return totalLogins;
   }

   public long getTotalDeaths() {
      return totalDeaths;
   }

   public long getTotalDiscoveries() {
      return totalDiscoveries;
   }

   public long getPvPKills() {
      return pvpKills;
   }

   public long getPvPDeaths() {
      return pvpDeaths;
   }

   public boolean isOnline() {
      return world != null;
   }

   public Optional<String> getWorld() {
      return Optional.ofNullable(world);
   }

   @com.busted_moments.core.api.internal.Request.Definition(route = "https://thesimpleones.net/api/player?q=%s", ratelimit = RateLimit.NONE, cache_length = 15)
   public static class Request extends GetRequest<Player> {
      public Request(String player) {
         super(player);
      }

      public Request(UUID player) {
         this(player.toString());
      }


      @org.jetbrains.annotations.Nullable
      @Override
      protected Player get(Json json) {
         return json.wrap(Player::new);
      }
   }
}
