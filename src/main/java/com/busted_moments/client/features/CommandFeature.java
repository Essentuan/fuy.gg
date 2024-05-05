package com.busted_moments.client.features;

import com.busted_moments.core.Feature;
import com.essentuan.acf.core.annotations.Command;
import com.essentuan.acf.fabric.core.FabricCommandBuilder;
import com.essentuan.acf.fabric.core.client.FabricClientCommandLoader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.wynntils.mc.event.CommandSentEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;

import static com.busted_moments.client.Client.CLASS_SCANNER;

@Feature.Definition(name = "Command Loader", required = true)
public class CommandFeature extends Feature {
   private FabricClientCommandLoader COMMAND_LOADER;

   @Override
   protected void onInit() {
      COMMAND_LOADER = new FabricCommandBuilder.Client()
              .inPackage(() -> CLASS_SCANNER.getTypesAnnotatedWith(Command.class).stream()
                      .filter(c -> !c.getPackageName().contains("subcommands"))
                      .toList())
              .withArguments(() -> new ArrayList<>(CLASS_SCANNER.getSubTypesOf(ArgumentType.class)))
              .build();
   }

   @SubscribeEvent
   private static void onCommandSend(CommandSentEvent event) {
      event.setCanceled(new com.essentuan.acf.fabric.core.client.events.CommandSentEvent(event.getCommand()).post());
   }
}
