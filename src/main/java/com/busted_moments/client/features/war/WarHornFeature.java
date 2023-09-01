package com.busted_moments.client.features.war;

import com.busted_moments.client.config.providers.sound.SoundProvider;
import com.busted_moments.client.util.SoundUtil;
import com.busted_moments.core.Feature;
import com.busted_moments.core.config.Config;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.chat.type.RecipientType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.regex.Matcher;

import static com.busted_moments.client.models.war.timer.TimerModel.ATTACK_PATTERN;

@Config.Category("War")
@Feature.Definition(name = "Autoskip Dialogue", description = "Automatically skips dialogue")
public class WarHornFeature extends Feature {
    @Dropdown(title = "Selected Sound", options = SoundProvider.class)
    private SoundEvent selected = SoundEvents.ANVIL_LAND;

    @Value("Sound volume")
    @Tooltip("The volume of the sound")
    private static Float volume = 1.0F;

    @Value("Pitch")
    @Tooltip("The pitch of the sound")
    private static Float pitch = 1.0F;
    @SubscribeEvent
    public void onMessage(ChatMessageReceivedEvent event) {
        if (event.getRecipientType() != RecipientType.GUILD) return;

        Matcher matcher = event.getOriginalStyledText().getMatcher(ATTACK_PATTERN, PartStyle.StyleType.NONE);
        if (!matcher.matches()) return;

        SoundUtil.playAmbient(selected, pitch, volume);
    }
}
