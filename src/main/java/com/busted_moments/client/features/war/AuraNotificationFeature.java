package com.busted_moments.client.features.war;

import com.busted_moments.client.config.providers.sound.SoundProvider;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.core.Feature;
import com.busted_moments.core.config.Config;
import com.wynntils.core.text.StyledText;
import com.wynntils.mc.event.SubtitleSetTextEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Config.Category("War")
@Feature.Definition(name = "Aura Sound Notification", description = "Plays a sound if an aura is coming")
public class AuraNotificationFeature extends Feature {
    @Dropdown(title = "Selected Sound", options = SoundProvider.class)
    private SoundEvent selected = SoundEvents.ANVIL_LAND;

    @Value("Sound volume")
    @Tooltip("The volume of the sound")
    private static Float volume = 1.0F;

    @Value("Pitch")
    @Tooltip("The pitch of the sound")
    private static Float pitch = 1.0F;

    private static final StyledText AURA_TITLE = StyledText.fromString("§4§n/!\\§7 Tower §6Aura");

    @SubscribeEvent
    public void onSubtitle(SubtitleSetTextEvent event) {
        if (!StyledText.fromComponent(event.getComponent()).equals(AURA_TITLE)) return;
        SoundUtil.playAmbient(selected, pitch, volume);
    }
}
