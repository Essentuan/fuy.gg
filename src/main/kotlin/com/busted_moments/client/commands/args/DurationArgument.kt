package com.busted_moments.client.commands.args

import com.essentuan.acf.core.command.CommandArgument
import com.essentuan.acf.core.command.arguments.Argument
import com.essentuan.acf.fabric.core.client.FabricClientBuildContext
import com.essentuan.acf.util.CommandException
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.essentuan.esl.fetch.annotations.duration
import net.essentuan.esl.time.TimeUnit
import net.essentuan.esl.time.duration.Duration
import net.essentuan.esl.time.duration.seconds
import net.minecraft.network.chat.Component
import java.awt.SystemColor.text
import java.util.concurrent.CompletableFuture

class DurationArgument(
    argument: CommandArgument<*, FabricClientBuildContext>
) : Argument<Duration, FabricClientBuildContext>(argument) {
    override fun parse(reader: StringReader): Duration =
        Companion.parse(reader, true)

    override fun <S : Any?> suggests(p0: CommandContext<S>, p1: SuggestionsBuilder): CompletableFuture<Suggestions> {
        Companion.suggests(StringReader(p1.remaining), true) { p1.suggest(it) }

        return p1.buildFuture()
    }

    companion object {
        fun parse(reader: StringReader, spaces: Boolean = false): Duration {
            var duration = 0.seconds

            var done = false

            while (reader.canRead() && !done) {
                while (reader.canRead()) {
                    val next = reader.peek()
                    if (next == ' ')
                        if (spaces) reader.skip() else return duration
                    else
                        break
                }

                var builder = StringBuilder()
                val number = reader.readDouble()

                while (reader.canRead()) {
                    val next = reader.peek()
                    if (next == ' ')
                        if (spaces) reader.skip() else return duration
                    else
                        break
                }

                while (reader.canRead()) {
                    val c = reader.peek()

                    if (c == ' ') {
                        if (spaces)
                            break
                        else {
                            done = true
                            break;
                        }
                    } else
                        if (StringReader.isAllowedNumber(c))
                            break;

                    reader.skip()
                    builder.append(c)
                }

                val str = builder.toString()
                val (_, unit) = TimeUnit.entries
                    .asSequence()
                    .flatMap { sequenceOf(it.plural() to it, it.singular() to it, it.suffix() to it) }
                    .sortedByDescending { (it, _) -> it.length }
                    .firstOrNull { (it, _) -> str.lowercase() == it.lowercase() }
                    ?: throw CommandException.SyntaxError(reader, "Unknown unit!")

                duration += Duration(number, unit)
            }

            return duration
        }

        inline fun suggests(reader: StringReader, spaces: Boolean = false, suggests: (text: String) -> Unit) {
            val builder = StringBuilder()

            while (reader.canRead()) {
                val next = reader.read()

                if (next == ' ')
                    if (spaces) continue else return

                if (StringReader.isAllowedNumber(next))
                    builder.clear()
                else
                    builder.append(next)
            }

            TimeUnit.entries
                .asSequence()
                .flatMap { sequenceOf(it.plural(), it.singular(), it.suffix()) }
                .sortedByDescending { it.length }
                .filter { it.contains(builder, ignoreCase = true) }
                .forEach { suggests(it.lowercase()) }
        }
    }
}