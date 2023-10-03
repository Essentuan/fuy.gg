package com.busted_moments.client.features.raids;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Date;

import static com.busted_moments.client.features.raids.RaidsCommon.*;
import static net.minecraft.ChatFormatting.*;

@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "Nexus of Light Raid", description = "")
public class NOLRaid extends Feature {
    @Hidden("NOL personal best")
    public static Duration NOLPB = Duration.FOREVER;

    @Hidden("Rooms Complete")
    private static boolean room1Complete = false;

    @SubscribeEvent
    private static void titleSetEvent(TitleSetTextEvent event){
       String msg = event.getComponent().getString();
       if(inRaid && raidType != "NOL") return;
       if (msg.equals("§fOrphion's Nexus of §lLight") && !inRaid){
        inRaid = true;
        raidStartTime = new Date();
        raidType = "NOL";
       }else if(msg.equals("§4Raid Failed!") && inRaid){
           raidOver();
       }else if (msg.equals("§6§lⓞ ⓡ ⓟ ⓗ ⓘ ⓞ ⓝ")) {
           TIMES.add(Duration.since(raidStartTime).toMinutes());
       }else if (msg.equals("§a§lRAID COMPLETED!") && inRaid && raidType == "NOL"){
        inRaid = false;
            boolean isPB = Duration.since(raidStartTime).lessThan(NOLPB);
           TextBuilder builder = TextBuilder.of("Room 1: ", LIGHT_PURPLE).next()
                   .append(timeCalc(TIMES.get(0)), AQUA)
                   .line()
                   .append("Boss Start: ", LIGHT_PURPLE).next()
                   .append(timeCalc(TIMES.get(1)), AQUA)
                   .line()
                   .append("Raid Time: ", LIGHT_PURPLE).next()
                   .append(timeCalc(Duration.since(raidStartTime).toSeconds()), AQUA)
                   .line();

           if (isPB){
               builder.append(
                       "New Personal Best!", GOLD);
                NOLPB = Duration.since(raidStartTime);
           }else{
               builder.append("Your Personal Best: ", LIGHT_PURPLE)
                       .append(timeCalc(NOLPB.toSeconds()), AQUA);
           }
           ChatUtil.send(builder);

           RaidsCommon.raidOver();
       }
    }

    @SubscribeEvent
    private static void subtitleSetEvent(SubtitleSetTextEvent event){
        String msg = event.getComponent().getString();
        if (!msg.equals("§7[Challenge complete]") || !inRaid || raidType != "NOL" || roomCount>0) return;
        TIMES.add(Duration.since(raidStartTime).toSeconds());
        roomCount = roomCount+1;
    }

}
