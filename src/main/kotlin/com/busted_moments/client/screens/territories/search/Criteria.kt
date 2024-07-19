package com.busted_moments.client.screens.territories.search

import com.busted_moments.client.commands.args.war.WarFilter.Operator
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.mojang.brigadier.StringReader
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.instance
import java.util.function.Predicate

private fun providers() =
    Reflections.types
        .subtypesOf(Criteria.Provider::class)
        .map { it.instance }
        .filterNotNull()
        .flatten()
        .flatMap { it.names.map { name -> name.lowercase() to it } }
        .toMap()

fun interface Criteria : Predicate<TerritoryData> {
    interface Builder {
        val names: Array<String>

        val operators: Array<Operator>

        val suggestions: Array<String>

        fun parse(reader: StringReader, operator: Operator): Criteria

        companion object : Provider {
            private val builders = Reflections.types
                .subtypesOf(Builder::class)
                .map { it.instance }
                .filterNotNull()
                .toList()

            override fun iterator(): Iterator<Builder> =
                builders.iterator()
        }
    }

    interface Provider : Iterable<Builder> {
        companion object : Map<String, Builder> by providers() {
            val builders: List<Builder> = values.asSequence().distinct().toList()
        }
    }
}