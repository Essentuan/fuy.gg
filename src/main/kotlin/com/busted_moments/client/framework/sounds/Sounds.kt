package com.busted_moments.client.framework.sounds

import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.other.lock
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.resources.sounds.SoundInstance
import net.minecraft.client.resources.sounds.SoundInstance.Attenuation
import net.minecraft.core.Holder
import net.minecraft.core.Position
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f

object Sounds : Collection<SoundEvent> {
    val WAR_HORN = register("wynntils:war.horn")

    override val size: Int
        get() = BuiltInRegistries.SOUND_EVENT.size()

    @Suppress("ReplaceSizeZeroCheckWithIsEmpty")
    override fun isEmpty(): Boolean =
        size == 0

    operator fun plusAssign(location: String) {
        register(location)
    }

    operator fun plusAssign(location: ResourceLocation) {
        register(location)
    }

    fun register(location: String): SoundEvent =
        register(ResourceLocation.parse(location))

    fun register(location: ResourceLocation): SoundEvent =
        lock {
            get(location) ?: Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                location,
                SoundEvent.createVariableRangeEvent(location)
            )
        }

    operator fun get(location: ResourceLocation) =
        BuiltInRegistries.SOUND_EVENT[location]

    operator fun get(location: String) =
        BuiltInRegistries.SOUND_EVENT[ResourceLocation.parse(location)]

    override fun contains(element: SoundEvent): Boolean =
        BuiltInRegistries.SOUND_EVENT.containsKey(element.location)

    override fun iterator(): Iterator<SoundEvent> =
        BuiltInRegistries.SOUND_EVENT.iterator()

    override fun containsAll(elements: Collection<SoundEvent>): Boolean {
        for (e in elements)
            if (e !in this)
                return false

        return true
    }

    fun SoundEvent.play(
        source: SoundSource = SoundSource.MASTER,
        volume: Float = 1.0f,
        speed: Float = 1.0f,
        random: RandomSource = SoundInstance.createUnseededRandom(),
        looping: Boolean = false,
        delay: Int = 0,
        attenuation: Attenuation = Attenuation.NONE,
        x: Double = 0.0,
        y: Double = 0.0,
        z: Double = 0.0,
        relative: Boolean = true
    ) {
        SimpleSoundInstance(
            location,
            source,
            speed,
            volume,
            random,
            looping,
            delay,
            attenuation,
            x,
            y,
            z,
            relative
        ).play()
    }

    fun Holder.Reference<SoundEvent>.play(
        source: SoundSource = SoundSource.MASTER,
        volume: Float = 1.0f,
        speed: Float = 1.0f,
        random: RandomSource = SoundInstance.createUnseededRandom(),
        looping: Boolean = false,
        delay: Int = 0,
        attenuation: Attenuation = Attenuation.NONE,
        x: Double = 0.0,
        y: Double = 0.0,
        z: Double = 0.0,
        relative: Boolean = true
    ) = value().play(
        source,
        volume,
        speed,
        random,
        looping,
        delay,
        attenuation,
        x,
        y,
        z,
        relative
    )

    fun SoundInstance.play() {
        if (delay == 0)
            mc().soundManager.play(this)
        else
            mc().soundManager.playDelayed(this, delay)
    }
}