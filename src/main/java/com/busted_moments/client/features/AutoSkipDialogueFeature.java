package com.busted_moments.client.features;

import com.busted_moments.client.util.PacketUtil;
import com.busted_moments.core.Feature;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.time.TimeUnit;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import com.wynntils.handlers.chat.type.RecipientType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;

@Feature.Definition(name = "Autoskip Dialogue", description = "Automatically skips dialogue")
public class AutoSkipDialogueFeature extends Feature {
    private static final String PRESS_TO_CONTINUE_TEXT = "Press SHIFT to continue";

    @SubscribeEvent
    public void NpcDialogEvent(NpcDialogEvent event) {
        if (event.getType() != NpcDialogueType.NORMAL) return;

        PacketUtil.Action(Action.PRESS_SHIFT_KEY);

        Heartbeat.schedule(() -> PacketUtil.Action(Action.RELEASE_SHIFT_KEY), 50, TimeUnit.MILLISECONDS);
    }

    @SubscribeEvent
    public void ChatReceivedEvent(ChatMessageReceivedEvent event){
        if (event.getRecipientType() != RecipientType.INFO || !event.getStyledText().trim().contains(PRESS_TO_CONTINUE_TEXT, PartStyle.StyleType.NONE)) return;
        
        PacketUtil.Action(Action.PRESS_SHIFT_KEY);

        Heartbeat.schedule(() -> PacketUtil.Action(Action.RELEASE_SHIFT_KEY), 50, TimeUnit.MILLISECONDS);
    }
}
