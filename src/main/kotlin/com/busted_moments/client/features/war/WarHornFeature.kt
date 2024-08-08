package com.busted_moments.client.features.war

import com.busted_moments.client.framework.config.annotations.Category
import com.busted_moments.client.framework.config.entries.dropdown.Dropdown
import com.busted_moments.client.framework.config.entries.value.Value
import com.busted_moments.client.framework.events.Subscribe
import com.busted_moments.client.framework.features.Feature
import com.busted_moments.client.framework.features.Replaces
import com.busted_moments.client.framework.sounds.Sounds
import com.busted_moments.client.framework.sounds.Sounds.play
import com.busted_moments.client.models.territories.timers.events.TimerEvent
import com.wynntils.features.embellishments.WarHornFeature
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource

@Category("War")
@Replaces(WarHornFeature::class)
object WarHornFeature : Feature() {
    @Dropdown("Selected Sound")
    private var sound: SoundEvent = Sounds.WAR_HORN

    @Value("Source")
    private var source: SoundSource = SoundSource.AMBIENT

    @Value("Sound volume")
    private var volume: Float = 1.0f

    @Value("Speed")
    private var speed: Float = 1.0f

    @Subscribe
    private fun TimerEvent.Enqueued.on() {
        if (source == TimerEvent.Source.CHAT)
            sound.play(
                source = this@WarHornFeature.source,
                volume = volume,
                speed = speed
            )
    }
}