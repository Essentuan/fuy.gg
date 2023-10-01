package com.busted_moments.client.features.raids;

import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.config.Config;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


@Config.Category("Raids")
@Default(State.ENABLED)
@Feature.Definition(name = "Nest Of The Grootsleng Raid", description = "")
public class NOGRaid extends Feature {
    @Hidden("NOG personal best")
    private static double NOGPB = 999.99;

    /*
    @SubscribeEvent
    private static void onChatRecieved(ChatMessageReceivedEvent event){
    String msg = event.getMessage().getString();
    LOGGER.info("getMessage: "+msg);
    }
    */

}
