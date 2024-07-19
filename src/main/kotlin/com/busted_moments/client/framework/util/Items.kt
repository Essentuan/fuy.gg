package com.busted_moments.client.framework.util

import com.busted_moments.client.framework.text.Text
import com.wynntils.core.text.StyledText
import com.wynntils.utils.mc.McUtils.mc
import com.wynntils.utils.mc.McUtils.player
import net.minecraft.network.chat.FormattedText
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag

object Items {
    val ItemStack.tooltip: List<StyledText>
        get() {
            return getTooltipLines(
                Item.TooltipContext.of(mc().level ?: return emptyList()),
                player(),
                TooltipFlag.NORMAL
            ).map { Text(it) }
        }
}