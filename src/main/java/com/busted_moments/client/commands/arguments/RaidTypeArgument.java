package com.busted_moments.client.commands.arguments;

import com.busted_moments.client.models.raids.RaidType;
import com.essentuan.acf.core.command.CommandArgument;
import com.essentuan.acf.core.command.arguments.MappedArgument;
import com.essentuan.acf.core.command.arguments.annotations.ArgumentDefinition;
import com.essentuan.acf.core.command.arguments.parameters.exceptions.ArgumentParameterException;
import com.essentuan.acf.fabric.core.client.FabricClientBuildContext;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ArgumentDefinition(RaidType.class)
public class RaidTypeArgument extends MappedArgument<RaidType, FabricClientBuildContext> {
   private final static Map<String, RaidType> RAID_TYPES =
           Stream.of(RaidType.values())
                   .collect(Collectors.toUnmodifiableMap(
                      RaidType::friendlyName,
                      Function.identity()
                   ));

   public RaidTypeArgument(CommandArgument<?, FabricClientBuildContext> argument) throws ArgumentParameterException {
      super(argument);
   }

   @Override
   protected Map<?, RaidType> getMap() {
      return RAID_TYPES;
   }

   @Override
   protected String map(RaidType raidType) {
      return raidType.friendlyName();
   }
}
