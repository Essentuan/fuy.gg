package com.busted_moments.core.http.api.player;

import com.busted_moments.core.http.AbstractRequest;
import com.busted_moments.core.http.GetRequest;
import com.busted_moments.core.http.RateLimit;
import com.busted_moments.core.http.api.Printable;
import com.busted_moments.core.http.api.guild.GuildType;
import com.busted_moments.core.http.models.wynncraft.player.PlayerModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.time.Duration;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public interface Player extends PlayerType, Collection<Character> {
   Rank rank();

   StoreRank storeRank();

   Date firstJoin();

   Date lastJoin();

   Duration playtime();

   Optional<Guild> guild();

   int countWars();

   int totalLevel();

   int mobsKilled();

   int chestsFound();

   Map<Dungeon, Integer> dungeons();

   Map<Raid, Integer> raids();

   int completedQuests();

   int pvpKills();

   int pvpDeaths();

   Character get(UUID uuid);

   boolean contains(UUID uuid);

   Map<Leaderboard.Type, Integer> ranking();

   boolean isPublic();

   interface Guild extends GuildType {
      com.busted_moments.core.http.api.guild.Guild.Rank rank();
   }

   enum Rank implements Printable {
      ADMINISTRATOR("Administrator"),
      WEB_DEV("Web Dev"),
      MODERATOR("Moderator"),
      BUILDER("Builder"),
      ITEM("Item Team"),
      GAME_MASTER("Game Master"),
      CMD("CMD"),
      MUSIC("Music"),
      HYBRID("Hybrid"),
      MEDIA("Media"),
      ART("Art"),
      PLAYER("Player");

      private final String prettyPrint;

      Rank(String readableString) {
         this.prettyPrint = readableString;
      }

      public String prettyPrint() {
         return prettyPrint;
      }
   }

   interface Leaderboard {
      enum Type {
         COMBAT_GLOBAL_LEVEL("combatGlobalLevel"),
         MERGED_GLOBAL_LEVEL("mergedGlobalLevel"),
         COMBAT_SOLO_LEVEL("combatSoloLevel"),
         PROFESSIONS_GLOBAL_LEVEL("professionsGlobalLevel"),
         WOODCUTTING_LEVEL("woodcuttingLevel"),
         PROFESSIONS_SOLO_LEVEL("professionsSoloLevel"),
         FARMING_LEVEL("farmingLevel"),
         MERGED_SOLO_LEVEL("mergedSoloLevel"),
         MINING_LEVEL("miningLevel"),
         FISHING_LEVEL("fishingLevel"),
         JEWELING_LEVEL("jewelingLevel"),
         SCRIBING_LEVEL("scribingLevel"),
         TAILORING_LEVEL("tailoringLevel"),
         WEAPON_SMITHING_LEVEL("weaponsmithingLevel"),
         WOODWORKING_LEVEL("woodworkingLevel"),
         COOKING_LEVEL("cookingLevel"),
         ALCHEMISM_LEVEL("alchemismLevel"),
         ARMOURING_LEVEL("armouringLevel"),
         IRONMAN_LEVEL("ironmanLevel"),
         ULTIMATE_IRONMAN_LEVEL("ultimateIronmanLevel"),
         CRAFTSMAN_LEVEL("craftsmanLevel"),
         HARDCORE_LEVEL("hardcoreLevel"),
         HARDCORE_LEGACY_LEVEL("hardcoreLegacyLevel"),
         HUNTED_LEVEL("huntedLevel"),
         HUIC_LEVEL("huicLevel"),
         HUICH_LEVEL("huichLevel"),
         HUICH_CONTENT("huichContent");

         private final String type;

         Type(String type) {
            this.type = type;
         }
      }
   }

   @AbstractRequest.Definition(
           route = "https://thesimpleones.net/api/player?q=%s",
           ratelimit = RateLimit.NONE,
           cache_length = 2
   )
   class Request extends GetRequest<Player> {
      public Request(String username) {
         super(username);
      }

      public Request(UUID uuid) {
         super(uuid);
      }

      @Nullable
      @Override
      protected Player get(Json json) {
         return json.wrap(PlayerModel::new);
      }
   }

   default boolean add(Character o) {
      throw new UnsupportedOperationException();
   }

   default boolean remove(Object o) {
      throw new UnsupportedOperationException();
   }

   default boolean addAll(Collection<? extends Character> c) {
      throw new UnsupportedOperationException();
   }

   default boolean removeAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   @Override
   default boolean removeIf(Predicate<? super Character> filter) {
      throw new UnsupportedOperationException();
   }

   default boolean retainAll(Collection<?> c) {
      throw new UnsupportedOperationException();
   }

   default void clear() {
      throw new UnsupportedOperationException();
   }
}
