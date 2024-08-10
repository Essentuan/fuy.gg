package com.busted_moments.client.framework.config.encoders

import com.busted_moments.client.framework.artemis.registry
import com.wynntils.utils.mc.McUtils.mc
import net.essentuan.esl.encoding.StringBasedEncoder
import net.essentuan.esl.other.Base64
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.nbt.TagParser
import net.minecraft.world.item.ItemStack
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type
import kotlin.jvm.optionals.getOrNull

object ItemStackEncoder : StringBasedEncoder<ItemStack>() {
    override fun encode(
        obj: ItemStack,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): String? {
        BuiltInRegistries.REGISTRY

        return Base64.encode(
            obj.save(mc().registry).toString())
    }

    override fun decode(
        obj: String,
        flags: Set<Any>,
        type: Class<*>,
        element: AnnotatedElement,
        vararg typeArgs: Type
    ): ItemStack? {
        return ItemStack.parse(
            mc().registry,
            TagParser.parseTag(if (obj[0] == '{') obj else Base64.decode(obj))
        ).getOrNull()
    }
}