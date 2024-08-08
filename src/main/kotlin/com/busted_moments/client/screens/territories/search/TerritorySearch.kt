package com.busted_moments.client.screens.territories.search

import com.busted_moments.client.commands.args.war.WarFilter
import com.busted_moments.client.framework.render.screen.elements.TextInputElement
import com.busted_moments.client.framework.text.SizedString
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.models.territories.TerritoryModel
import com.busted_moments.client.models.territories.eco.TerritoryData
import com.mojang.brigadier.StringReader
import com.wynntils.core.text.StyledText
import java.util.function.Predicate

object TerritorySearch : TextInputElement.Preprocessor, Predicate<TerritoryData> {
    private var search: String = ""
    private var criteria = mutableListOf<Criteria>()

    var input: SizedString = SizedString.EMPTY
        private set

    override fun test(t: TerritoryData): Boolean =
        (criteria.isEmpty() || criteria.all { it.test(t) }) && (search.isEmpty() || TerritoryModel.matches(
            t.name,
            search
        ))

    override fun TextInputElement.process(input: SizedString): StyledText = Text {
        search = ""
        criteria.clear()

        val reader = StringReader(input.toString())

        while (reader.canRead()) {
            reader.apply {
                var space = false
                val builder = StringBuilder()

                while (canRead()) {
                    val char = read()

                    if (char == ' ') {
                        space = true
                        break
                    }

                    val operator = WarFilter.Operator[char, reader]
                    when {
                        operator != null -> {
                            var name = builder.toString()
                            val not = if (name.isNotEmpty() && name[0] == '^' && name.length > 1) {
                                name = name.substring(1)
                                true
                            } else false

                            val factory = Criteria.Provider[name.lowercase()]
                            val start = cursor

                            if (factory == null || operator !in factory.operators) {
                                if (not)
                                    +"^".red

                                +name.red
                                +operator.symbol.red
                                +reader.readString().red

                                return@apply
                            }

                            try {
                                criteria += factory.parse(reader, operator).let {
                                    if (not)
                                        Criteria { territory -> !it.test(territory) }
                                    else
                                        it
                                }

                                if (not)
                                    +"^".gold

                                +name.aqua
                                +operator.symbol.yellow
                                +reader.string.substring(start..<cursor).lightPurple
                            } catch (ex: Exception) {
                                if (not)
                                    +"^".red

                                +name.red
                                +operator.symbol.red
                                +reader.string.substring(start..<cursor).red
                            }

                            return@apply
                        }

                        else -> builder.append(char)
                    }
                }

                builder.toString().let {
                    if (it.isNotEmpty()) {
                        search += "${if (search.isNotEmpty()) " " else ""}$it"
                        +it.white
                    }

                    if (space)
                        +" "
                }
            }
        }

        this@TerritorySearch.input = input
    }

    fun reset() {
        search = ""
        criteria.clear()

        input = SizedString.EMPTY
    }
}