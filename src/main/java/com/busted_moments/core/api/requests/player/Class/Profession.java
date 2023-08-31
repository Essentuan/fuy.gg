package com.busted_moments.core.api.requests.player.Class;

import com.busted_moments.core.json.template.JsonTemplate;

public class Profession extends JsonTemplate {
    @Entry private ProfessionType type;
    @Entry private double progress;
    @Entry private int level;


    public ProfessionType getType() {
        return type;
    }

    public double getProgress() {
        return progress;
    }

    public int getLevel() {
        return level;
    }
}
