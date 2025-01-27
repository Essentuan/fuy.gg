package com.busted_moments.client.framework.sounds

import net.essentuan.esl.encoding.StringBasedEncoder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvent
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrNull

object SoundWriter : StringBasedEncoder<SoundEvent>() {
    override fun decode(
        obj: String,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): SoundEvent? {
        return BuiltInRegistries.SOUND_EVENT.get(
            ResourceLocation.parse(obj)
        ).getOrNull()?.value()
    }

    override fun encode(
        obj: SoundEvent,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): String =
        obj.location.toString()
}