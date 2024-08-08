package com.busted_moments.client.framework.config.entries.dropdown.providers

import com.busted_moments.client.framework.config.entries.dropdown.Dropdown
import com.busted_moments.client.framework.text.Text
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent

object SoundProvider : Dropdown.Provider<SoundEvent> {
    override fun get(key: String): SoundEvent? {
        return BuiltInRegistries.SOUND_EVENT.get(
            ResourceLocation.parse(
                key
            )
        )
    }

    override fun iterator(): Iterator<SoundEvent> =
        BuiltInRegistries.SOUND_EVENT.iterator()

    override fun name(entry: SoundEvent): Component {
        return Text.component(entry.location.toString())
    }
}