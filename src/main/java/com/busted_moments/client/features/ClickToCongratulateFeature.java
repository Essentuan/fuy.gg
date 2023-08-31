package com.busted_moments.client.features;

import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.core.Default;
import com.busted_moments.core.Feature;
import com.busted_moments.core.State;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.time.TimeUnit;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static com.busted_moments.client.util.ChatUtil.component;

@Default(State.ENABLED)
@Feature.Definition(name = "Click To Congratulate", description = "Click to send a congratulations message!")
public class ClickToCongratulateFeature extends Feature {

    @Value("Congratulations Message")
    @Tooltip("What message to use to congratulate people")
    private static String CongratsMSG = "Congratulations!";

    @SubscribeEvent
    public void ChatMessageReceivedEvent(ChatMessageReceivedEvent event){
        String message = event.getMessage().getString();
        if (!message.contains("[!] Congratulations to")) return;
        String PlayerName = message.split(" ")[3];

        Heartbeat.schedule(() -> {
            ChatUtil.message(
                    (component(
                            "Click to congratulate %s!".formatted(PlayerName)
                    ).setStyle(
                            Style.EMPTY.withClickEvent(
                                            new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/msg %s %s".formatted(PlayerName, CongratsMSG)))
                                    .withColor(ChatFormatting.AQUA)))
            );
        }, 10, TimeUnit.MILLISECONDS);
    }
}
