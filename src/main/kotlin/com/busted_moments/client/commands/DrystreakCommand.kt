@file:Command("drystreak")

package com.busted_moments.client.commands

import com.busted_moments.client.features.LootrunDryStreakFeature
import com.busted_moments.client.framework.text.FUY_PREFIX
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.framework.text.Text.invoke
import com.busted_moments.client.framework.text.Text.send
import com.busted_moments.client.framework.text.TextPart
import com.busted_moments.client.framework.util.Numbers.escapeCommas
import com.busted_moments.client.framework.util.Numbers.toCommaString
import com.essentuan.acf.core.annotations.Argument
import com.essentuan.acf.core.annotations.Command
import com.essentuan.acf.core.annotations.Default
import com.essentuan.acf.core.annotations.Subcommand
import com.mojang.brigadier.context.CommandContext
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.HoverEvent.ItemStackInfo
import kotlin.math.ceil
import kotlin.math.max

private const val ITEMS_PER_PAGE = 5

@Default
private fun CommandContext<*>.default() {
    FUY_PREFIX {
        +"You've gone ".lightPurple
        +LootrunDryStreakFeature.dry.toCommaString().escapeCommas().gold
        +" pulls without finding a ".lightPurple
        +"Mythic".darkPurple
        +".".lightPurple
    }.send()
}

@Subcommand("average")
private fun CommandContext<*>.average() {
    FUY_PREFIX {
        +"You average ".lightPurple

        +(LootrunDryStreakFeature.pulls
            .asSequence()
            .map { it.pulls } + sequenceOf(LootrunDryStreakFeature.dry))
            .average()
            .toInt()
            .toCommaString().escapeCommas().gold

        +" pulls between ".lightPurple
        +"Mythics".darkPurple
        +".".lightPurple
    }.send()
}

@Subcommand("pulls page")
private fun CommandContext<*>.pullsPage(
    @Argument("page") page: Int
) {
    FUY_PREFIX {
        newLine()
        newLine()

        val pulls = LootrunDryStreakFeature.pulls

        if (pulls.isEmpty()) {
            center {
                +"There is nothing to display".white.underline
                +"".reset
            }

            newLine()
            newLine()
        } else {
            pulls
                .asReversed()
                .drop(page * ITEMS_PER_PAGE)
                .take(ITEMS_PER_PAGE)
                .forEach {
                    center {
                        val name = Text(it.item.hoverName)

                        for (part in name) {
                            +TextPart(part).underline.onHover(
                                HoverEvent.Action.SHOW_ITEM,
                                ItemStackInfo(it.item)
                            )
                        }
                    }

                    newLine()
                    newLine()

                    center {
                        +"After ".lightPurple
                        +it.pulls.toCommaString().escapeCommas().gold
                        +" pulls.".lightPurple
                    }

                    newLine()
                    newLine()
                }
        }

        center {
            val pages = ceil(pulls.size / ITEMS_PER_PAGE.toDouble()).toInt()

            +"⋘".let {
                if (page > 0)
                    it.white.onClick(ClickEvent.Action.RUN_COMMAND, "/drystreak pulls page ${page - 1}")
                else
                    it.darkGray.strikethrough
            }

            +"   ".reset
            +(page + 1).toString().white
            +"/".gray
            +max(pages, 1).toString().white
            +"   "

            +"⋙".let {
                if (page < (pages - 1))
                    it.white.onClick(ClickEvent.Action.RUN_COMMAND, "/drystreak pulls page ${page + 1}")
                else
                    it.darkGray.strikethrough
            }
        }
    }.send()
}

@Subcommand("pulls")
private fun CommandContext<*>.pulls() =
    pullsPage(0)
