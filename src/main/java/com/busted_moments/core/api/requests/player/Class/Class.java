package com.busted_moments.core.api.requests.player.Class;

import com.busted_moments.core.json.template.JsonTemplate;

import java.util.*;

public class Class extends JsonTemplate {
    @Entry
    private UUID uuid;
    @Entry
    private ClassType classType;
    @Entry
    private int combatLevel;
    @Entry
    private int totalLevel;
    @Entry
    private double levelProgress;
    @Entry
    private Map<String, Long> dungeonCompletions;
    @Entry
    private long totalDungeonCompletions;
    @Entry
    private Map<String, Long> raidCompletions;
    @Entry
    private long totalRaidCompletions;
    @Entry
    private List<String> questsCompleted;
    @Entry
    private long playtime;
    @Entry
    private long totalMobsKilled;
    @Entry
    private long totalLogins;
    @Entry
    private long totalDeaths;
    @Entry
    private List<GameplayModifiers> gameplayModifiers;
    @Entry
    private Map<SkillPointType, Integer> skillPointAssignments;
    @Entry
    private Map<ProfessionType, Profession> professions;
    @Entry
    private long totalDiscoveries;
    @Entry
    private boolean preEconomyUpdate;

    public UUID getUUID() {
        return uuid;
    }

    public ClassType getType() {
        return classType;
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public int getTotalLevel() {
        return totalLevel;
    }

    public double getLevelProgress() {
        return levelProgress;
    }

    public Map<String, Long> getDungeonCompletions() {
        return dungeonCompletions;
    }

    public long getTotalDungeonCompletions() {
        return totalDungeonCompletions;
    }

    public Map<String, Long> getRaidCompletions() {
        return raidCompletions;
    }

    public long getTotalRaidCompletions() {
        return totalRaidCompletions;
    }

    public List<String> getQuestsCompleted() {
        return questsCompleted;
    }

    public long getTotalMobsKilled() {
        return totalMobsKilled;
    }

    public long getTotalLogins() {
        return totalLogins;
    }

    public long getTotalDeaths() {
        return totalDeaths;
    }

    public long getPlaytime() {
        return playtime;
    }

    public List<GameplayModifiers> getGameplayModifiers() {
        return gameplayModifiers;
    }

    public Map<SkillPointType, Integer> getSkillPointAssignments() {
        return skillPointAssignments;
    }

    public Collection<Profession> getProfessions() {
        return getProfessionsAsMap().values();
    }

    public Map<ProfessionType, Profession> getProfessionsAsMap() {
        return professions;
    }

    public long getTotalDiscoveries() {
        return totalDiscoveries;
    }

    public boolean isPreEconomyUpdate() {
        return preEconomyUpdate;
    }
}