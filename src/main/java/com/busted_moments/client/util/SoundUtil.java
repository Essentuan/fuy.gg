package com.busted_moments.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class SoundUtil {
    public static void playAmbient(SoundEvent sound, float pitch, float volume) {
        play(SimpleSoundInstance.forLocalAmbience(sound, volume, pitch));
    }

    public static void play(SoundEvent sound, SoundSource source, float volume, float pitch) {
        play(new SimpleSoundInstance(
                sound.getLocation(), source, pitch, volume, SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true
        ));
    }

    public static void play(SoundInstance instance) {
        Minecraft.getInstance().getSoundManager().play(instance);
    }
}
