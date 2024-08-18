package com.busted_moments.client.framework.wynntils.config

import com.busted_moments.client.framework.wynntils.wynntils
import com.busted_moments.client.framework.wynntils.esl
import com.busted_moments.client.framework.config.Config
import com.busted_moments.client.framework.config.Storage
import com.wynntils.core.persisted.PersistedOwner
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color
import net.essentuan.esl.reflections.extensions.simpleString

class ColorConfig(
    model: Storage,
    entry: Config.Entry<Color>,
    owner: PersistedOwner,
) : LinkedConfig<Color>(
    model,
    entry,
    owner,
    CustomColor::class.java,
    when(val value = entry.default) {
        is Int -> CustomColor.fromInt(value).esl
        is Color -> value.wynntils.esl
        else -> throw IllegalArgumentException("Cannot cast ${value?.javaClass?.simpleString() ?: "null"} to ${CustomColor::class.simpleString()}!")
    }
) {
    override fun get(): Color {
        return super.get().wynntils.esl
    }
}