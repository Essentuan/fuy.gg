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
import static com.busted_moments.client.features.raids.RaidsCommon.roomCount;
import static com.wynntils.utils.mc.McUtils.mc;
import static net.minecraft.ChatFormatting.*;

@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "The Canyon Colossus", description = "")
public class TCCRaid extends Feature {
    @Hidden("tcc personal best")
    public static Duration TCCPB = Duration.FOREVER;

    private static final Position room2EndPos = new Vec3(11841, 20, 3985);
    private static final Position raidFailPos = new Vec3(665, 49, -4448);

    @SubscribeEvent
    private static void titleSetEvent(TitleSetTextEvent event){
        String msg = event.getComponent().getString();
        if(inRaid && raidType != "TCC") return;
        if (msg.equals("§3The Canyon Colossus") && !inRaid) {
            inRaid = true;
            raidStartTime = new Date();
            raidType = "TCC";
        }else if (msg.equals("§8§kThe defense system") && roomCount == 3) {
            roomComplete();
        }else if (msg.equals("§4§k! SYSTEM FAILURE !") && roomCount == 4){
            roomComplete();
        }else if (msg.equals("§a§lRAID COMPLETED!") && inRaid){
            inRaid = false;
            boolean isPB = Duration.since(raidStartTime).lessThan(TCCPB);
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
                    .append("Phase 1: ", LIGHT_PURPLE).next()
                    .append(timeCalc(TIMES.get(4)), AQUA)
                    .line()
                    .append("Phase 2 & 3: ", LIGHT_PURPLE).next()
                    .append(timeCalc(TIMES.get(5)), AQUA)
                    .line()
                    .append("Raid Time: ", LIGHT_PURPLE).next()
                    .append(timeCalc(Duration.since(raidStartTime).toSeconds()), AQUA)
                    .line();

            if (isPB){
                builder.append(
                        "New Personal Best!", GOLD);
                TCCPB = Duration.since(raidStartTime);
            }else{
                builder.append("Your Personal Best: ", LIGHT_PURPLE)
                        .append(timeCalc(TCCPB.toSeconds()), AQUA);
            }
            ChatUtil.send(builder);

            RaidsCommon.raidOver();
        }
    }

    @SubscribeEvent
    private static void subtitleSetEvent(SubtitleSetTextEvent event){
        String msg = event.getComponent().getString().toLowerCase();
        if (!inRaid || raidType != "TCC") return;
        if (msg.equals("§7[challenge complete]")) {
            roomComplete();
        }else if(msg.equals("§bto enter the colossus") && roomCount == 5){
            roomComplete();
        }
    }

    @SubscribeEvent
    private static void onTick(TickEvent event){
        if (!inRaid || raidType != "TCC") return;
        Vec3 playerPos = mc().player.position();

        if (playerPos.closerThan(room2EndPos, 50) && roomCount == 1){
            roomComplete();
        }else if(playerPos.closerThan(raidFailPos, 50)){
            raidOver();
        }
    }

}
