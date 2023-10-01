package com.busted_moments.client.features.raids;

import com.busted_moments.core.config.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Config.Category("Raids")
public class RaidsCommon extends Config {

    @Hidden("inRaid")
    public static boolean inRaid = false;

    @Hidden("RaidStartTime")
    public static Date raidStartTime = new Date();
    @Hidden("Times")
    public static List<Double> TIMES = new ArrayList<>();

    public static void raidOver(){
        inRaid = false;
    }
}
