package com.busted_moments.client.features;

import com.busted_moments.client.util.PacketUtil;
import com.busted_moments.core.Feature;
import com.busted_moments.core.heartbeat.Heartbeat;
import com.busted_moments.core.time.ChronoUnit;
import com.wynntils.models.npcdialogue.event.NpcDialogueProcessingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import static net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket.Action;

@Feature.Definition(name = "Autoskip Dialogue", description = "Automatically skips dialogue")
public class AutoSkipDialogueFeature extends Feature {
    private static final String PRESS_TO_CONTINUE_TEXT = "Press SHIFT to continue";

    @SubscribeEvent
    public void NpcDialogEvent(NpcDialogueProcessingEvent.Post event) {
        PacketUtil.Action(Action.PRESS_SHIFT_KEY);

        Heartbeat.schedule(() -> PacketUtil.Action(Action.RELEASE_SHIFT_KEY), 50, ChronoUnit.MILLISECONDS);
    }
}
