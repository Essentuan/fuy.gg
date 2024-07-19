package com.busted_moments.client.screens.territories.search.criteria

import com.busted_moments.client.commands.args.war.WarFilter
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.models.territories.eco.Upgrade
import com.busted_moments.client.screens.territories.search.Criteria
import com.mojang.brigadier.StringReader
import net.essentuan.esl.string.extensions.camelCase

object UpgradeProvider : Criteria.Provider {
    private data class Filter(
        val upgrade: Upgrade,
        val amount: Int,
        val operator: WarFilter.Operator
    ) : Criteria {
        override fun test(t: TerritoryData): Boolean =
            operator.test(t.upgrades[upgrade]?.first ?: 0, amount)
    }

    private class Builder(
        val upgrade: Upgrade
    ) : Criteria.Builder {
        override val names: Array<String>
            get() = arrayOf(upgrade.getName().camelCase(first = false, separator = "", '_', ' ', '-'))
        override val operators: Array<WarFilter.Operator>
            get() = enumValues()
        override val suggestions: Array<String>
            get() = emptyArray()

        override fun parse(reader: StringReader, operator: WarFilter.Operator): Criteria =
            Filter(upgrade, reader.readInt(), operator)
    }

    override fun iterator(): Iterator<Criteria.Builder> = iterator {
        for (upgrade in Upgrade.entries)
            yield(Builder(upgrade))
    }
}