package com.busted_moments.client.screens.territories.search.criteria

import com.busted_moments.client.commands.args.war.WarFilter
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.screens.territories.search.Criteria
import com.mojang.brigadier.StringReader
import java.util.function.Predicate

class IsCriteria(
    val type: Type
) : Criteria {
    override fun test(t: TerritoryData): Boolean =
        type.test(t)

    companion object : Criteria.Builder {
        override val names: Array<String>
            get() = arrayOf("is")
        override val operators: Array<WarFilter.Operator>
            get() = arrayOf(WarFilter.Operator.IS)

        override val suggestions: Array<String>
            get() = arrayOf("hq", "connections", "conn", "external", "ext")

        override fun parse(reader: StringReader, operator: WarFilter.Operator): Criteria =
            IsCriteria(enumValueOf(reader.readString().uppercase()))
    }

    enum class Type : Predicate<TerritoryData> {
        HQ {
            override fun test(t: TerritoryData): Boolean =
                t.hq
        },
        CONNECTION {
            override fun test(t: TerritoryData): Boolean =
                t.absoluteRoute == null || t.absoluteRoute!!.size <= 1

        },
        CONN {
            override fun test(t: TerritoryData): Boolean =
                CONNECTION.test(t)
        },
        EXTERNAL {
            override fun test(t: TerritoryData): Boolean =
               t.absoluteRoute == null || t.absoluteRoute!!.size <= 3
        },
        EXT {
            override fun test(t: TerritoryData): Boolean =
                EXTERNAL.test(t)
        }
    }
}