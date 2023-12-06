package com.busted_moments.core.http.models.wynncraft.player;

import com.busted_moments.core.http.api.player.Character;
import com.busted_moments.core.http.api.player.Dungeon;
import com.busted_moments.core.http.api.player.Profession;
import com.busted_moments.core.http.api.player.Raid;
import com.busted_moments.core.http.api.player.SkillPoint;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.json.Json;
import com.busted_moments.core.time.ChronoUnit;
import com.busted_moments.core.time.Duration;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class CharacterModel extends BaseModel implements Character {
   @Key
   private UUID uuid;
   @Key
   private Type type;
   @Key
   @Null
   private String nickname;
   @Key
   private int level;
   @Key
   private long xp;
   @Key
   private int xpPercent;
   @Key
   private int totalLevel;
   @Key
   private int wars;
   @Key
   @Unit(ChronoUnit.HOURS)
   private Duration playtime;
   @Key
   private long mobsKilled;
   @Key
   private long chestsFound;
   @Key
   private int logins;
   @Key
   private int deaths;
   @Key
   private int discoveries;
   @Key("gamemode")
   @Final
   private Set<Modifier> modifiers = Set.of();
   @Key
   @Final
   private Map<SkillPoint, Integer> skillPoints = Map.of();
   @Key
   @Final
   private Map<Profession.Type, ProfessionsModel> professions = Map.of();

   @Key("dungeons")
   @Final
   private Map<Dungeon, Integer> dungeons = Map.of();
   @Key("raids")
   @Final
   private Map<Raid, Integer> raids = Map.of();
   @Key
   @Final
   private Set<String> quests = Set.of();

   @Override
   public BaseModel load(Json json) {
      return super.load(json);
   }

   @Override
   public UUID uuid() {
      return uuid;
   }

   @Override
   public Type type() {
      return type;
   }

   @Override
   public Optional<String> nickname() {
      return Optional.ofNullable(nickname);
   }

   @Override
   public int level() {
      return level;
   }

   @Override
   public long xp() {
      return xp;
   }

   @Override
   public int xpPercent() {
      return xpPercent;
   }

   @Override
   public int totalLevel() {
      return totalLevel;
   }

   @Override
   public int wars() {
      return wars;
   }

   @Override
   public Duration playtime() {
      return playtime;
   }

   @Override
   public long mobsKilled() {
      return mobsKilled;
   }

   @Override
   public long chestsFound() {
      return chestsFound;
   }

   @Override
   public int logins() {
      return logins;
   }

   @Override
   public int deaths() {
      return deaths;
   }

   @Override
   public int discoveries() {
      return discoveries;
   }

   @Override
   public Set<Modifier> modifiers() {
      return modifiers;
   }

   @Override
   public Map<SkillPoint, Integer> skillPoints() {
      return skillPoints;
   }

   @Override
   @SuppressWarnings("rawtypes")
   public Map<Profession.Type, Profession> professions() {
      return (Map<Profession.Type, Profession>) (Map) professions;
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
   public Set<String> completedQuests() {
      return quests;
   }
}
