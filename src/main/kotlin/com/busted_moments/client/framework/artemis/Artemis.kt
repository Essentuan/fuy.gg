package com.busted_moments.client.framework.artemis

import com.wynntils.core.components.Managers
import com.wynntils.utils.colors.CustomColor
import net.essentuan.esl.color.Color

val CustomColor.esl: Color
    get() = this as Color

val Color.artemis: CustomColor
    get() {
        if (this is CustomColor)
            return this

        return CustomColor(red, green, blue, alpha)
    }

object Ticks {
    fun schedule(ticks: Int = 0, block: Runnable) =
        Managers.TickScheduler.scheduleLater(block, ticks)
}