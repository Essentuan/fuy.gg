package com.busted_moments.client.commands.args.war

import com.busted_moments.client.buster.TerritoryList
import com.busted_moments.client.commands.args.DurationArgument
import com.busted_moments.client.commands.args.war.WarFilter.Builder
import com.busted_moments.client.commands.args.war.WarFilter.Group.Type
import com.busted_moments.client.commands.args.war.WarFilter.Operator
import com.busted_moments.client.framework.text.Text
import com.busted_moments.client.models.territories.TerritoryModel
import com.busted_moments.client.models.territories.war.War
import com.essentuan.acf.core.command.CommandArgument
import com.essentuan.acf.core.command.arguments.Argument
import com.essentuan.acf.core.command.arguments.annotations.ArgumentDefinition
import com.essentuan.acf.fabric.core.client.FabricClientBuildContext
import com.essentuan.acf.util.Arguments.suggests
import com.essentuan.acf.util.CommandException
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.CommandSyntaxException
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.essentuan.esl.fetch.annotations.duration
import net.essentuan.esl.isFail
import net.essentuan.esl.isPresent
import net.essentuan.esl.reflections.Reflections
import net.essentuan.esl.reflections.extensions.instance
import net.essentuan.esl.string.extensions.bestMatch
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.extensions.timeSince
import net.essentuan.esl.unsafe
import net.minecraft.network.chat.Component
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate


private fun builders(): Map<String, Builder> {
    return Reflections.types
        .subtypesOf(Builder::class)
        .map { it.instance }.filterNotNull()
        .associateBy { it.name }
}

interface WarFilter : Predicate<War.Results> {
    enum class Operator(val symbol: String, val tooltip: String) {
        IS(":", "Is operator") {
            override fun <T : Comparable<T>> test(predicate: T, value: T): Boolean =
                predicate == value
        },
        LESS_THAN("<", "Less than operator") {
            override fun <T : Comparable<T>> test(predicate: T, value: T): Boolean =
                predicate < value

        },
        LESS_THAN_OR_EQUAL("<=", "Less than or equal operator") {
            override fun <T : Comparable<T>> test(predicate: T, value: T): Boolean =
                predicate <= value
        },
        GREATER_THAN(">", "Greater than operator") {
            override fun <T : Comparable<T>> test(predicate: T, value: T): Boolean =
                predicate > value
        },
        GREATER_THAN_OR_EQUAL(">=", "Greater than or equal operator") {
            override fun <T : Comparable<T>> test(predicate: T, value: T): Boolean =
                predicate >= value
        };

        abstract fun <T: Comparable<T>> test(predicate: T, value: T): Boolean

        companion object {
            operator fun get(c: Char, reader: StringReader): Operator? {
                for (operator in entries.sortedByDescending { it.symbol.length }) {
                    val symbol = operator.symbol

                    if (symbol.length == 1) {
                        if (symbol[0] == c)
                            return operator
                    } else {
                        if (!reader.canRead(symbol.length - 1))
                            continue

                        if ("$c${
                                buildString {
                                    for (i in 1..<symbol.length)
                                        append(reader.peek(i - 1))
                                }
                            }" == symbol
                        ) {
                            reader.cursor+= (symbol.length - 1)

                            return operator
                        }
                    }
                }

                return null;
            }
        }
    }

    companion object {
        fun parse(reader: StringReader, root: Boolean = true): WarFilter {
            val input = reader.remaining

            val out = mutableListOf<WarFilter>()
            var current = Group(mutableListOf(), Type.AND).also {
                out += it
            }

            var isOrAllowed = false
            var isNotAllowed = true

            var or = false
            var not = false

            val builder = StringBuilder()

            fun push(filter: WarFilter) {
                val final = filter.let { if (not) Not(it) else it }
                val type = if (or) Type.OR else Type.AND

                if (type == current.type)
                    current.filters += final
                else if (current.filters.size < 2) {
                    current.type = type
                    current.filters += final
                } else {
                    current = Group(mutableListOf(), type).also {
                        out += it
                        it.filters += final
                    }
                }

                isOrAllowed = true
                isNotAllowed = true

                or = false
                not = false
            }

            while (reader.canRead()) {
                when (val c = reader.read()) {
                    ' ' -> Unit

                    '|' -> {
                        if (!isOrAllowed || or)
                            throw CommandException.SyntaxError(reader, "Unexpected OR!")

                        or = true
                    }

                    '^' -> {
                        if (!isNotAllowed || not)
                            throw CommandException.SyntaxError(reader, "Unexpected NOT!")

                        not = true
                    }

                    '(' -> {
                        if (builder.isNotEmpty())
                            throw CommandException.SyntaxError(reader, "Unexpected opening bracket!")
                        else
                            push(parse(reader, root = false))
                    }

                    ')' -> {
                        if (root || builder.isNotEmpty())
                            throw CommandException.SyntaxError(reader, "Unexpected closing bracket!")
                        else
                            return Group(out, Type.AND)
                    }

                    else -> {
                        val operator = Operator[c, reader]
                        if (operator != null) {
                            reader.cursor += operator.symbol.length - 1

                            if (builder.isEmpty())
                                throw CommandException.SyntaxError(reader, "Unexpected operator!")

                            val filter = builder.toString()
                            builder.clear()

                            val filterBuilder = Builder[filter] ?: throw CommandException.SyntaxError(
                                reader,
                                reader.cursor - filter.length - 1,
                                "Unknown filter!"
                            )

                            if (operator !in filterBuilder.operators)
                                throw CommandException.SyntaxError(
                                    reader,
                                    reader.cursor - operator.symbol.length,
                                    "Unsupported operator!"
                                )

                            push(filterBuilder.parse(reader, operator))

                            continue
                        }

                        if (c !in 'a'..'z')
                            throw CommandException.SyntaxError(reader, "Unexpected character!")

                        builder.append(c)
                    }
                }
            }

            if (not || or || builder.isNotEmpty() || !root)
                throw CommandException.SyntaxError(reader, "Unexpected end of input!")

            return Compiled(input, out)
        }
    }

    interface Builder {
        val name: String

        val description: String?
            get() = null

        val operators: Array<Operator>

        fun parse(reader: StringReader, operator: Operator): WarFilter

        /**
         * @return `true` if the input was valid
         */
        fun Completion.suggests(operator: WarFilter.Operator): Boolean

        companion object : Map<String, Builder> by builders()
    }

    data class Completion(
        private var suggestions: SuggestionsBuilder,
        val start: Int = suggestions.start,
        val reader: StringReader
    ) {
        fun bump() {
            suggestions = suggestions.createOffset(start + reader.cursor)
        }

        fun offset(int: Int) {
            suggestions = suggestions.createOffset(suggestions.start + int)
        }

        fun at(int: Int) {
            suggestions = suggestions.createOffset(start + int)
        }

        fun suggest(text: String) {
            suggestions.suggest(text)
        }

        fun suggest(int: Int) {
            suggestions.suggest(int)
        }

        fun suggest(text: String, tooltip: String) {
            suggestions.suggest(text, Text.component(tooltip))
        }

        fun suggest(int: Int, tooltip: String) {
            suggestions.suggest(int, Text.component(tooltip))
        }

        fun suggest(text: String, tooltip: Component) {
            suggestions.suggest(text, tooltip)
        }

        fun suggest(int: Int, tooltip: Component) {
            suggestions.suggest(int, tooltip)
        }

        fun build() =
            suggestions.buildFuture()
    }

    data class Compiled(
        private val input: String,
        private val filters: List<WarFilter>
    ) : WarFilter {
        override fun test(war: War.Results): Boolean = when {
            filters.isEmpty() -> true
            filters.size == 1 -> filters[0].test(war)
            else -> filters.all { it.test(war) }
        }

        override fun toString(): String =
            input
    }

    data class Group(
        val filters: MutableList<WarFilter>,
        var type: Type
    ) : WarFilter {
        override fun test(war: War.Results): Boolean = when {
            filters.isEmpty() -> true
            filters.size == 1 -> filters[0].test(war)
            type == Type.AND -> filters.all { it.test(war) }
            else -> filters.any { it.test(war) }
        }

        enum class Type {
            AND,
            OR;
        }
    }

    data class Not(
        val filter: WarFilter
    ) : WarFilter {
        override fun test(war: War.Results): Boolean =
            !filter.test(war)
    }

    data class Territory(
        val string: String
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            TerritoryModel.matches(t.territory, string)

        companion object : Builder {
            override val name: String
                get() = "territory"
            override val operators: Array<Operator>
                get() = arrayOf(Operator.IS)

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Territory(reader.readString())

            override fun Completion.suggests(operator: Operator): Boolean {
                val builder = StringBuilder()

                var char: Char? = null
                var escaped: Boolean = false

                while (reader.canRead()) {
                    val c = reader.read()

                    when {
                        escaped -> builder.append(c)
                        c == '\\' -> {
                            escaped = true
                        }

                        c == '\'' -> when (char) {
                            null -> if (builder.isEmpty()) {
                                char = c
                            } else {
                                return false
                            }

                            '\"' -> {
                                break;
                            }

                            else -> return false
                        }

                        c == '"' -> when (char) {
                            null -> if (builder.isEmpty()) {
                                char = c
                            } else {
                                return false
                            }

                            '"' -> return true
                            else -> return false
                        }

                        c == ' ' -> {
                            if (char == null)
                                return true
                            else
                                builder.append(c)
                        }

                        else -> builder.append(c)
                    }
                }

                TerritoryList.asSequence()
                    .map { "\"${it.name}\"" }
                    .filter { it.contains(builder, ignoreCase = true) }
                    .forEach { suggest(it) }

                return false
            }
        }
    }

    data class Past(
        val duration: Duration
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            t.started.timeSince() < duration

        companion object : Builder {
            override val name: String
                get() = "past"
            override val operators: Array<Operator>
                get() = arrayOf(Operator.IS)

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Past(DurationArgument.parse(reader))

            override fun Completion.suggests(operator: Operator): Boolean {
                DurationArgument.suggests(reader) { suggest(it) }

                return reader.peek(-1) == ' '
            }
        }
    }

    data class Since(
        val duration: Duration,
        val operator: Operator
    ) : WarFilter {
        override fun test(war: War.Results): Boolean =
            operator.test(war.started.timeSince(), duration)

        companion object : Builder {
            override val name: String
                get() = "since"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Since(DurationArgument.parse(reader), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                DurationArgument.suggests(reader) { suggest(it) }

                return reader.peek(-1) == ' '
            }
        }
    }

    data class Health(
        val health: Long,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.initial.health, health)

        companion object : Builder {
            override val name: String
                get() = "health"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Health(reader.readLong(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readLong()
                }.isPresent()
            }
        }
    }

    data class FinalHealth(
        val health: Long,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.final.health, health)

        companion object : Builder {
            override val name: String
                get() = "finalhealth"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                FinalHealth(reader.readLong(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readLong()
                }.isPresent()
            }
        }
    }

    data class Defense(
        val defense: Float,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.initial.defense, defense)

        companion object : Builder {
            override val name: String
                get() = "defense"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Defense(reader.readFloat(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readFloat()
                }.isPresent()
            }
        }
    }

    data class FinalDefense(
        val defense: Float,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.final.defense, defense)

        companion object : Builder {
            override val name: String
                get() = "finaldefense"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                FinalDefense(reader.readFloat(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readFloat()
                }.isPresent()
            }
        }
    }

    data class DamageMin(
        val damage: Int,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.initial.damageMin, damage)

        companion object : Builder {
            override val name: String
                get() = "damagemin"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                DamageMin(reader.readInt(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readInt()
                }.isPresent()
            }
        }
    }

    data class FinalDamageMin(
        val damage: Int,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.final.damageMin, damage)

        companion object : Builder {
            override val name: String
                get() = "finaldamagemin"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                FinalDamageMin(reader.readInt(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readInt()
                }.isPresent()
            }
        }
    }

    data class DamageMax(
        val damage: Int,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.initial.damageMax, damage)

        companion object : Builder {
            override val name: String
                get() = "damagemax"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                DamageMax(reader.readInt(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readInt()
                }.isPresent()
            }
        }
    }

    data class FinalDamageMax(
        val damage: Int,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.final.damageMax, damage)

        companion object : Builder {
            override val name: String
                get() = "finaldamagemax"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                FinalDamageMax(reader.readInt(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readInt()
                }.isPresent()
            }
        }
    }

    data class AttackSpeed(
        val atkSpeed: Float,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.initial.attackSpeed, atkSpeed)

        companion object : Builder {
            override val name: String
                get() = "attackspeed"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                AttackSpeed(reader.readFloat(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readFloat()
                }.isPresent()
            }
        }
    }

    data class FinalAttackSpeed(
        val atkSpeed: Float,
        val operator: Operator
    ) : WarFilter {
        override fun test(t: War.Results): Boolean =
            operator.test(t.final.attackSpeed, atkSpeed)

        companion object : Builder {
            override val name: String
                get() = "finalattackspeed"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                FinalAttackSpeed(reader.readFloat(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readFloat()
                }.isPresent()
            }
        }
    }

    data class Length(
        val duration: Duration,
        val operator: Operator
    ) : WarFilter {
        override fun test(war: War.Results): Boolean =
            operator.test(war.duration, duration)

        companion object : Builder {
            override val name: String
                get() = "length"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Length(DurationArgument.parse(reader), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                DurationArgument.suggests(reader) { suggest(it) }

                return reader.peek(-1) == ' '
            }
        }
    }

    data class Dps(
        val dps: Double,
        val operator: Operator
    ) : WarFilter {
        override fun test(war: War.Results): Boolean =
            operator.test(war.dps, dps)

        companion object : Builder {
            override val name: String
                get() = "dps"
            override val operators: Array<Operator>
                get() = arrayOf(
                    Operator.IS,
                    Operator.LESS_THAN,
                    Operator.LESS_THAN_OR_EQUAL,
                    Operator.GREATER_THAN,
                    Operator.GREATER_THAN_OR_EQUAL
                )

            override fun parse(reader: StringReader, operator: Operator): WarFilter =
                Dps(reader.readDouble(), operator)

            override fun Completion.suggests(operator: Operator): Boolean {
                return unsafe {
                    reader.readDouble()
                }.isPresent()
            }
        }
    }
}

@ArgumentDefinition(WarFilter::class)
class WarFilterArgument(
    arg: CommandArgument<*, FabricClientBuildContext>
) : Argument<WarFilter, FabricClientBuildContext>(arg) {
    override fun parse(reader: StringReader): WarFilter =
        WarFilter.parse(reader)

    override fun <S : Any?> suggests(
        p0: CommandContext<S?>,
        base: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val reader = StringReader(base.remaining)
        val completion = WarFilter.Completion(base, reader = reader)

        var or = false
        var not = true

        var default = true

        val builder = StringBuilder()

        while (reader.canRead()) {
            val c = reader.read().lowercaseChar()

            when (c) {
                ' ' -> Unit
                '^' -> {
                    not = false
                }

                '|' -> {
                    or = false
                }

                '(' -> {
                    if (builder.isNotEmpty()) {
                        default = false
                        break
                    }

                    or = false
                }

                ')' -> {
                    if (builder.isNotEmpty()) {
                        default = false
                        break
                    }

                    or = false
                }

                else -> {
                    val operator = Operator[c, reader]
                    if (operator != null) {
                        reader.cursor += operator.symbol.length - 1
                        val filter = builder.toString()

                        builder.clear()

                        completion.bump()

                        val filterBuilder = Builder[filter]

                        if (
                            filterBuilder == null ||
                            operator !in filterBuilder.operators ||
                            filterBuilder.run { completion.run { !suggests(operator) } }
                        ) {
                            default = false

                            break
                        }

                        or = true
                        not = true

                        continue
                    }

                    if (c !in 'a'..'z') {
                        default = false
                        break
                    }

                    builder.append(c)
                }
            }
        }

        if (default) {
            completion.at(reader.cursor - builder.length)

            if (not && builder.isEmpty())
                completion.suggest("^", Component.literal("Not operator; inverts the following filter"))

            if (or && builder.isEmpty())
                completion.suggest("|", Component.literal("Or operator"))

            for ((name, filterBuilder) in Builder) {
                if (!name.contains(builder, ignoreCase = true))
                    continue

                if (filterBuilder.description == null)
                    completion.suggest(name)
                else
                    completion.suggest(name, Text.component(filterBuilder.description!!))
            }
        }

        val filter = Builder[builder.toString().lowercase()]
        if (filter != null) {
            completion.bump()

            for (operator in filter.operators)
                completion.suggest(operator.symbol, operator.tooltip)
        }

        return completion.build()
    }
}