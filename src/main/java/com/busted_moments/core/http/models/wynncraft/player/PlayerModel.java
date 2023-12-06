package com.busted_moments.core.http.models.wynncraft.player;

import com.busted_moments.core.http.api.player.Character;
import com.busted_moments.core.http.api.player.Dungeon;
import com.busted_moments.core.http.api.player.Player;
import com.busted_moments.core.http.api.player.Raid;
import com.busted_moments.core.http.api.player.StoreRank;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.time.Duration;
import com.busted_moments.core.util.iterators.Iter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class PlayerModel extends BaseModel implements Player {
   @Key private String username;
   @Key private UUID uuid;
   @Key @Null private String world;
   @Key private Player.Rank rank;
   @Key private StoreRank supportRank;
   @Key private Date firstJoin;
   @Key private Date lastJoin;
   @Key("playtime") @Unit(ChronoUnit.HOURS) private Duration totalPlaytime;
   @Key @Null private GuildTypeModel guild;

   @Key("globalData.wars") private int wars;
   @Key("globalData.totalLevels") private int totalLevel;
   @Key("globalData.killedMobs") private int killedMobs;
   @Key("globalData.chestsFound") private int chestsFound;
   @Key("globalData.dungeons") @Final private Map<Dungeon, Integer> dungeons = Map.of();
   @Key("globalData.raids") @Final private Map<Raid, Integer> raids = Map.of();
   @Key("globalData.completedQuests") private int completedQuests;

   @Key("globalData.pvp.kills") private int pvpKills;
   @Key("globalData.pvp.deaths") private int pvpDeaths;

   @Key @Final private Map<UUID, CharacterModel> characters = Map.of();
   @Key @Final private Map<Player.Leaderboard.Type, Integer> ranking = Map.of();
   @Key private boolean publicProfile;

   @Override
   public String username() {
      return username;
   }

   @Override
   public UUID uuid() {
      return uuid;
   }

   @Override
   public Optional<String> world() {
      return Optional.ofNullable(world);
   }

   @Override
   public Rank rank() {
      return rank;
   }

   @Override
   public StoreRank storeRank() {
      return supportRank;
   }

   @Override
   public Date firstJoin() {
      return firstJoin;
   }

   @Override
   public Date lastJoin() {
      return lastJoin;
   }

   @Override
   public Duration playtime() {
      return totalPlaytime;
   }

   @Override
   public Optional<Guild> guild() {
      return Optional.ofNullable(guild);
   }

   @Override
   public int countWars() {
      return wars;
   }

   @Override
   public int totalLevel() {
      return totalLevel;
   }

   @Override
   public int mobsKilled() {
      return killedMobs;
   }

   @Override
   public int chestsFound() {
      return chestsFound;
   }

   @Override
   public Map<Dungeon, Integer> dungeons() {
      return dungeons;
   }

   @Override
   public Map<Raid, Integer> raids() {
      return raids;
   }

   @Override
   public int completedQuests() {
      return completedQuests;
   }

   @Override
   public int pvpKills() {
      return pvpKills;
   }

   @Override
   public int pvpDeaths() {
      return pvpDeaths;
   }

   @Override
   public Character get(UUID uuid) {
      return characters.get(uuid);
   }

   @Override
   public boolean contains(UUID uuid) {
      return characters.containsKey(uuid);
   }

   @Override
   public Map<Leaderboard.Type, Integer> ranking() {
      return ranking;
   }

   @Override
   public boolean isPublic() {
      return publicProfile;
   }

   @Override
   public int size() {
      return characters.size();
   }

   @Override
   public boolean isEmpty() {
      return characters.isEmpty();
   }

   @Override
   public boolean contains(Object o) {
      return characters.containsValue(o);
   }

   @NotNull
   @Override
   public Iterator<Character> iterator() {
      return Iter.of(characters.values().iterator()).cast();
   }

   @NotNull
   @Override
   public Object[] toArray() {
      return characters.values().toArray();
   }

   @NotNull
   @Override
   public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
      return characters.values().toArray(a);
   }

   @Override
   public boolean containsAll(@NotNull Collection<?> c) {
      return characters.values().containsAll(c);
   }
}