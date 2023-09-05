package com.busted_moments.client.commands.arguments;

import com.busted_moments.core.time.Duration;
import com.essentuan.acf.core.command.CommandArgument;
import com.essentuan.acf.core.command.arguments.Argument;
import com.essentuan.acf.core.command.arguments.annotations.ArgumentDefinition;
import com.essentuan.acf.core.command.arguments.parameters.exceptions.ArgumentParameterException;
import com.essentuan.acf.fabric.core.client.FabricClientBuildContext;
import com.essentuan.acf.util.CommandException;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import java.util.concurrent.CompletableFuture;

@ArgumentDefinition(Duration.class)
public class DurationArgument extends Argument<Duration, FabricClientBuildContext> {
   public DurationArgument(CommandArgument<?, FabricClientBuildContext> argument) throws ArgumentParameterException {
      super(argument);
   }

   @Override
   protected <S> CompletableFuture<Suggestions> suggests(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
      return Suggestions.empty();
   }

   @Override
   public Duration parse(StringReader reader) throws CommandSyntaxException {
      return Duration.parse(reader.readString()).orElseThrow(
              () -> CommandException.SyntaxError("Could not parse duration")
      );
   }
}
