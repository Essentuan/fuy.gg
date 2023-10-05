package com.busted_moments.client.features.raids;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.busted_moments.core.text.TextBuilder;
import com.busted_moments.core.time.Duration;
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
@Feature.Definition(name = "The Nameless Anomaly", description = "")
public class TNARaid extends Feature {
    @Hidden("TNA personal best")
    public static Duration TNAPB = Duration.FOREVER;
    private static final Position room1Complete = new Vec3(24907, -7, -23984);
    private static final Position room2Complete = new Vec3(24914, 7, -23773);
    private static final Position room3Complete = new Vec3(24909, 7, -23560);
    private static final Position bossStartCoords = new Vec3(26620, 20, -22135);
    private static final Position raidFailPos = new Vec3(1120, 85, -853);

    @SubscribeEvent
    private static void titleSetEvent(TitleSetTextEvent event){
        String msg = event.getComponent().getString();
        if(inRaid && raidType != "TNA") return;
        if (msg.equals("§9§lThe §1§k§lNameless§9§l Anomaly") && !inRaid) {
            inRaid = true;
            raidStartTime = new Date();
            raidType = "TNA";
        }else if (msg.equals("§a§lRAID COMPLETED!") && inRaid){
            inRaid = false;
            boolean isPB = Duration.since(raidStartTime).lessThan(TNAPB);
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
                TNAPB = Duration.since(raidStartTime);
            }else{
                builder.append("Your Personal Best: ", LIGHT_PURPLE)
                        .append(timeCalc(TNAPB.toSeconds()), AQUA);
            }
            ChatUtil.send(builder);

            RaidsCommon.raidOver();
        }
    }

    @SubscribeEvent
    private static void onTick(TickEvent event){
        if (!inRaid || raidType != "TNA") return;
        Vec3 playerPos = mc().player.position();

        if ((playerPos.closerThan(room1Complete, 50) && roomCount == 0) ||
                (playerPos.closerThan(room2Complete, 50) && roomCount == 1) ||
                (playerPos.closerThan(room3Complete, 50) && roomCount == 2) ||
                (playerPos.closerThan(bossStartCoords, 50) && roomCount == 3)
        ){
            roomComplete();
        }else if(playerPos.closerThan(raidFailPos, 50)){
            raidOver();
        }
    }



}
