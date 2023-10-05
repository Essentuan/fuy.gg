package com.busted_moments.client.features.raids;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import com.wynntils.mc.event.TickEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import net.minecraft.core.Position;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Date;

import static com.busted_moments.client.features.raids.RaidsCommon.*;
import static com.wynntils.utils.mc.McUtils.mc;
import static net.minecraft.ChatFormatting.*;

@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "Nexus of Light Raid", description = "")
public class NOLRaid extends Feature {
    @Hidden("NOL personal best")
    public static Duration NOLPB = Duration.FOREVER;

    private static final Position room2FinishPos = new Vec3(11970, 33, 1817);
    private static final Position room3FinishPos = new Vec3(11966, 34, 1529);


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
           TIMES.add(Duration.since(raidStartTime).toSeconds());
       }else if (msg.equals("§a§lRAID COMPLETED!") && inRaid && raidType == "NOL"){
        inRaid = false;
        boolean isPB = Duration.since(raidStartTime).lessThan(NOLPB);

           TextBuilder builder = TextBuilder.of("Room 1: ", LIGHT_PURPLE).next()
                   .append(timeCalc(TIMES.get(0)), AQUA)
                   .line()
                   .append("Room 2: ", LIGHT_PURPLE).next()
                   .append(timeCalc(TIMES.get(1)), AQUA)
                   .line()
                   .append("Room 3: ", LIGHT_PURPLE).next()
                   .append(timeCalc(TIMES.get(2)), AQUA)
                   .line()
                   .append("Boss Start: ", LIGHT_PURPLE).next()
                   .append(timeCalc(TIMES.get(3)), AQUA)
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
        String msg = event.getComponent().getString().toLowerCase();
        if (!msg.equals("§7[challenge complete]") || !inRaid || raidType != "NOL" || roomCount>0) return;
        roomComplete();
    }

    @SubscribeEvent
    private static void onTick(TickEvent event){
        if (!inRaid || raidType != "NOL" || roomCount == 0 || mc().player == null) return;
        Vec3 playerPos = mc().player.position();

        if (playerPos.closerThan(room2FinishPos, 50) && inRaid && roomCount == 1){
            roomComplete();
        }else if(playerPos.closerThan(room3FinishPos, 50) && inRaid && roomCount == 2){
            roomComplete();
        }
    }

}
