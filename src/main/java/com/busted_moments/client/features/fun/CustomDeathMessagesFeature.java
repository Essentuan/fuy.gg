package com.busted_moments.client.features.fun;

import com.busted_moments.client.models.death.messages.DeathMessage;
import com.busted_moments.client.models.death.messages.Target;
import com.busted_moments.client.models.death.messages.events.DeathEvent;
import com.busted_moments.client.models.death.messages.functions.PlayFunction;
import com.busted_moments.client.models.death.messages.templates.FunctionalTemplate;
import com.busted_moments.client.models.death.messages.templates.PlayerTemplate;
import com.busted_moments.client.models.death.messages.types.DefaultMessage;
import com.busted_moments.core.Feature;
import com.busted_moments.core.config.Config;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import net.minecraft.util.RandomSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.busted_moments.client.models.war.WarModel.DEATH_REGEX;
import static com.wynntils.utils.mc.McUtils.player;

@Config.Category("Fun")
@Feature.Definition(name = "Custom Death Messages", description = "Replaces the default death messages")
public class CustomDeathMessagesFeature extends Feature {
   private static final RandomSource RANDOM_SOURCE = RandomSource.create();
   private static final Pattern PATTERN = Pattern.compile("^(?<strict>\\^?)\\[(?<players>.*)]:(?<template>.*)");

   @Value("Sound volume")
   @Tooltip("The volume of death sounds")
   public static float volume = 1.0F;

   @Value("Sound pitch")
   @Tooltip("The pitch of death sounds")
   public static float pitch = 1.0F;

   @Array("Death messages")
   @Tooltip({
           "Whenever a player dies it'll replace it with a random custom message",
           "",
           "Each message is the same as an info box (I.E you can use Wynntils functions)",
           "You can use {play(<sound>)} to play a sound when the message is displayed",
           "You can use {username} instead of {player} to display the username of the player that died",
           "You can use [<players...>] to make a message that only appears for specific players - Example:",
           "[essentuan; linnyflower]:{player} typed too much.",
           "You can add a ^ at the start to make that message appear every time a player dies - Example:",
           "^[essentuan]:{player}'s blood vessels finally burst."
   })
   private static List<String> templates;
   private static List<DeathMessage.Template> compiled = new ArrayList<>();
   private static Multimap<String, DeathMessage.Template> playerTemplates = MultimapBuilder.hashKeys().arrayListValues().build();

   static {
      templates = DeathMessage.DEFAULT.stream().map(message -> "{player} " + message).collect(Collectors.toList());

      templates.add("^[sphxia]:{player} made love to a cheep cheep. {play(\"fuy:cheep.cheep\")}");

      onUpdate(templates);
   }

   @Listener(field = "templates")
   private static void onUpdate(List<String> templates) {
      compiled.clear();
      playerTemplates.clear();

      templates.stream()
              .map(template -> {
                 Matcher matcher = PATTERN.matcher(template);
                 if (!matcher.matches())
                    return new FunctionalTemplate(template);

                 return new PlayerTemplate(matcher);
              }).forEach(template -> {
                 if (template instanceof PlayerTemplate playerTemplate && playerTemplate.isStrict())
                    playerTemplate.players().forEach(player -> playerTemplates.put(player, playerTemplate));
                 else
                    compiled.add(template);
              });
   }

   private boolean send(DeathEvent event, Collection<DeathMessage.Template> options) {
      if (options == null || options.isEmpty())
         return false;

      List<DeathMessage> results = options.stream()
              .filter(template -> !(template instanceof PlayerTemplate playerTemplate) || playerTemplate.players().contains(event.target().username().toLowerCase()))
              .map(t -> t.format(event.target()))
              .filter(message -> !message.build().isBlank())
              .toList();

      if (results.isEmpty())
         return false;

      event.setMessage(results.get(RANDOM_SOURCE.nextIntBetweenInclusive(0, results.size() - 1)));

      return true;
   }

   @SubscribeEvent
   public void onDeath(DeathEvent deathEvent) {
      if (!send(deathEvent, playerTemplates.get(deathEvent.target().username().toLowerCase())))
         send(deathEvent, compiled);
   }

   @SubscribeEvent
   public void onClientDeath(ChatMessageReceivedEvent event) {
      if (!event.getOriginalStyledText().matches(DEATH_REGEX, PartStyle.StyleType.NONE))
         return;

      PlayFunction.enabled = false;

      DeathEvent deathEvent = new DeathEvent(new DefaultMessage(
              new Target(
                      player().getGameProfile().getName(),
                      Optional.empty(),
                      StyledText.fromString(player().getGameProfile().getName())
              ),
              StyledText.fromUnformattedString("defaulted")
      ));

      onDeath(deathEvent);

      if (deathEvent.message() instanceof DefaultMessage || deathEvent.message().build().isBlank())
         return;

      PlayFunction.enabled = true;

      //This is just to play sounds for players if they have their owm death message
      deathEvent.message().build();
   }
}
