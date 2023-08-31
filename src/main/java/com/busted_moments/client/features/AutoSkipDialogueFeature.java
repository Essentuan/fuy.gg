package com.busted_moments.client.features;

import com.busted_moments.client.util.PacketUtil;
import com.busted_moments.core.Feature;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.time.TimeUnit;
import com.wynntils.handlers.chat.event.NpcDialogEvent;
import com.wynntils.handlers.chat.type.NpcDialogueType;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;

@Feature.Definition(name = "Autoskip Dialogue", description = "Automatically skips dialogue")
public class AutoSkipDialogueFeature extends Feature {
    @SubscribeEvent
    public void NpcDialogEvent(NpcDialogEvent event) {
        if (event.getType() != NpcDialogueType.NORMAL) return;

        PacketUtil.Action(Action.PRESS_SHIFT_KEY);

        Heartbeat.schedule(() -> PacketUtil.Action(Action.RELEASE_SHIFT_KEY), 50, TimeUnit.MILLISECONDS);
    }
}
