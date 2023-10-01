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
import static net.minecraft.ChatFormatting.AQUA;
import static net.minecraft.ChatFormatting.LIGHT_PURPLE;

@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "Nexus of Light Raid", description = "")
public class NOLRaid extends Feature {
    @Hidden("NOL personal best")
    private static Duration NOLPB = Duration.FOREVER;

    @SubscribeEvent
    private static void titleSetEvent(TitleSetTextEvent event){
       String msg = event.getComponent().getString();
       if (msg.equals("§fOrphion's Nexus of §lLight") && !inRaid){
        inRaid = true;
        raidStartTime = new Date();
       }else if(msg.equals("§4Raid Failed!") && inRaid){
           inRaid = false;
           TIMES.clear();
       }else if (msg.equals("§6§lⓞ ⓡ ⓟ ⓗ ⓘ ⓞ ⓝ")) {
           TIMES.add(Duration.since(raidStartTime).toMinutes());
       }else if (msg.equals("§a§lRAID COMPLETED!") && inRaid){
        inRaid = false;

           TextBuilder builder = TextBuilder.of("Room 1: ", LIGHT_PURPLE).next()
                   .append(TIMES.get(0), AQUA)
                   .line()
                   .append("Boss Start: ", LIGHT_PURPLE).next()
                   .append(TIMES.get(1), AQUA)
                   .line()
                   .append("Raid Time:", LIGHT_PURPLE).next()
                   .append(Duration.since(raidStartTime))
                   .line();

           ChatUtil.send(builder);

           if (Duration.since(raidStartTime).lessThan(NOLPB)){
                NOLPB = Duration.since(raidStartTime);
           }

           RaidsCommon.raidOver();
       }
    }

    @SubscribeEvent
    private static void subtitleSetEvent(SubtitleSetTextEvent event){
        String msg = event.getComponent().getString();
        if (msg.equals("§7[Challenge complete]") || !inRaid) return;
        TIMES.add(Duration.since(raidStartTime).toMinutes());
    }

}
