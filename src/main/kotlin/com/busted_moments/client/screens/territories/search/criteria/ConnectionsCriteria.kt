package com.busted_moments.client.screens.territories.search.criteria

import com.busted_moments.client.commands.args.war.WarFilter
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.screens.territories.search.Criteria
import com.mojang.brigadier.StringReader

data class ConnectionsCriteria(
    val amount: Int,
    val operator: WarFilter.Operator
) : Criteria {
    override fun test(t: TerritoryData): Boolean =
        operator.test(t.connections.size, amount)

    companion object : Criteria.Builder {
        override val names: Array<String>
            get() = arrayOf("connections", "cons")

        override val operators: Array<WarFilter.Operator>
            get() = enumValues()

        override val suggestions: Array<String>
            get() = emptyArray()

        override fun parse(reader: StringReader, operator: WarFilter.Operator): Criteria =
            ConnectionsCriteria(reader.readInt(), operator)
    }
}