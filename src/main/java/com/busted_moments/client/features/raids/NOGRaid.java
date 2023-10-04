package com.busted_moments.client.features.raids;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
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
@Feature.Definition(name = "Nest Of The Grootsleng Raid", description = "")
public class NOGRaid extends Feature {
    @Hidden("NOG personal best")
    private static Duration NOGPB = Duration.FOREVER;

    private static final Position room3EndPos = new Vec3(9282, 174, 3425);

    @SubscribeEvent
    private static void titleSetEvent(TitleSetTextEvent event){
        String msg = event.getComponent().getString();
        if(inRaid && raidType != "NOG") return;
        if (msg.equals("§2Nest of The Grootslangs") && !inRaid){
            inRaid = true;
            raidStartTime = new Date();
            raidType = "NOG";
        }else if(msg.equals("§4Raid Failed!") && inRaid){
            raidOver();
        }else if (msg.equals("§2§lThe Grootslang Wyrmlings")) {
            TIMES.add(Duration.since(raidStartTime).toSeconds());
        }else if (msg.equals("§a§lRAID COMPLETED!") && inRaid && raidType == "NOG"){
            inRaid = false;
            boolean isPB = Duration.since(raidStartTime).lessThan(NOGPB);
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
                NOGPB = Duration.since(raidStartTime);
            }else{
                builder.append("Your Personal Best: ", LIGHT_PURPLE)
                        .append(timeCalc(NOGPB.toSeconds()), AQUA);
            }
            ChatUtil.send(builder);

            RaidsCommon.raidOver();
        }
    }

    private static void subtitleSetEvent(SubtitleSetTextEvent event){
        String msg = event.getComponent().getString();
        if (!msg.equals("§7[Challenge complete]") || !inRaid || raidType != "NOL" || roomCount>1) return;
        TIMES.add(Duration.since(raidStartTime).toSeconds());
        roomCount = roomCount+1;
    }

    @SubscribeEvent
    private static void onTick(TickEvent event){
        if (!inRaid || raidType != "NOG" || 2>roomCount) return;
        Vec3 playerPos = mc().player.position();

        if (playerPos.closerThan(room3EndPos, 50) && inRaid){
            TIMES.add(Duration.since(raidStartTime).toSeconds());
            roomCount = roomCount+1;
        }
    }
}
