package com.busted_moments.client.features.raids;

import com.busted_moments.core.config.Config;
import com.busted_moments.core.render.overlay.Hud;
import com.busted_moments.core.time.Duration;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Config.Category("Raids")
public class RaidsCommon extends Config {
    @Hidden("inRaid")
    public static boolean inRaid = false;
    @Hidden("raidType")
    public static String raidType = "None";
    @Hidden("RaidStartTime")
    public static Date raidStartTime = new Date();
    @Hidden("Times")
    public static List<Double> TIMES = new ArrayList<>();
    @Hidden("RoomCount")
    public static int roomCount = 0;

    public static void raidOver(){
        inRaid = false;
        raidType = "None";
        TIMES.clear();
        roomCount = 0;
    }

    public static String timeCalc(double seconds){
        int Minutes = (int) Math.floor(seconds/60);
        int Seconds = (int) seconds%60;
        if (10>Seconds){
            return "%s:0%s".formatted(Minutes,Seconds);
        }else{
            return "%s:%s".formatted(Minutes,Seconds);
        }
    }

    public static void roomComplete(){
        TIMES.add(Duration.since(raidStartTime).toSeconds());
        roomCount = roomCount+1;
    }
}
