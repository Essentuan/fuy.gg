package com.busted_moments.client.screens.territories.search.criteria

import com.busted_moments.buster.api.Territory
import com.busted_moments.client.commands.args.war.WarFilter
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.busted_moments.client.screens.territories.search.Criteria
import com.mojang.brigadier.StringReader

object ProductionProvider : Criteria.Provider {
    private data class Filter(
        val resource: Territory.Resource,
        val amount: Int,
        val operator: WarFilter.Operator
    ) : Criteria {
        override fun test(t: TerritoryData): Boolean =
            operator.test(t.resources[resource]?.production ?: 0, amount)
    }

    private class Builder(
        val resource: Territory.Resource
    ) : Criteria.Builder{
        override val names: Array<String>
            get() = arrayOf(resource.print().lowercase())
        override val operators: Array<WarFilter.Operator>
            get() = enumValues()
        override val suggestions: Array<String>
            get() = emptyArray()

        override fun parse(reader: StringReader, operator: WarFilter.Operator): Criteria =
            Filter(resource, reader.readInt(), operator)
    }

    override fun iterator(): Iterator<Criteria.Builder> = iterator {
        for (resource in Territory.Resource.entries)
            yield(Builder(resource))
    }
}