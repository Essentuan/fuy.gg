package com.busted_moments.client.commands.args

import com.busted_moments.client.buster.GuildList
import com.essentuan.acf.core.command.CommandArgument
import com.essentuan.acf.core.command.arguments.Argument
import com.essentuan.acf.core.command.arguments.annotations.ArgumentDefinition
import com.essentuan.acf.fabric.core.client.FabricClientBuildContext
import com.essentuan.acf.util.Arguments
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import java.util.concurrent.CompletableFuture
import java.util.stream.Stream

@ArgumentDefinition(String::class)
class GuildArgument(
    argument: CommandArgument<*, FabricClientBuildContext>
) : Argument<String, FabricClientBuildContext>(argument) {
    override fun parse(reader: StringReader): String =
        reader.remaining.also {
            reader.cursor = reader.totalLength
        }

    override fun <S : Any?> suggests(
        p0: CommandContext<S>,
        p1: SuggestionsBuilder
    ): CompletableFuture<Suggestions> = Arguments.suggest(
        GuildList.stream().flatMap {
            Stream.of(it.name.trim(), it.tag.trim())
        },
        p1
    )
}