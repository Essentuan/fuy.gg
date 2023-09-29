package com.busted_moments.client.features;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.mc.event.TitleSetTextEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Default(State.ENABLED)
@Feature.Definition(name = "Raid Stats", description = "")
public class RaidStatsFeature extends Feature {
    private static boolean inRaid = false;

    @SubscribeEvent
    private static void TitleSetEvent(TitleSetTextEvent event){
        LOGGER.info(event.getComponent().getString());
    }

    @SubscribeEvent
    private static void onChatRecieved(ChatMessageReceivedEvent event){
        if (!event.getMessage().getString().equals("&4Raid Failed!") || !inRaid) return;
        inRaid = false; // death
    }
}
