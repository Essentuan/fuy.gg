package com.busted_moments.client.models.territory.eco.tributes;

import com.busted_moments.client.models.territory.eco.Patterns;
import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.client.util.ChatUtil;
import com.busted_moments.client.util.ContainerHelper;
import com.busted_moments.core.Model;
import com.wynntils.core.components.Models;
import com.wynntils.core.text.PartStyle;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.handlers.container.scriptedquery.QueryStep;
import com.wynntils.handlers.container.scriptedquery.ScriptedContainerQuery;
import com.wynntils.mc.event.ContainerSetContentEvent;
import com.wynntils.models.worlds.event.WorldStateEvent;
import com.wynntils.models.worlds.type.WorldState;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TributeModel extends Model {
   private static final Pattern DIPLOMACY_MENU = Pattern.compile("^(?<guild>.+): Diplomacy$");
   private static final Pattern TRIBUTES_SENT_PATTERN = Pattern.compile("^\\[INFO] (?<sender>.*) scheduled (. )?(?<amount>.+) (?<resource>.+) per hour to (?<receiver>.*)");
   private static final Pattern TRIBUTES_CANCELLED_PATTERN = Pattern.compile("^\\[INFO] (?<sender>.*) stopped scheduling (?<resource>.*) to (?<receiver>.*)");

   private static final Map<String, Tributes> TRIBUTES = new HashMap<>();

   @SubscribeEvent
   public void onWorldSwap(WorldStateEvent event) {
      if (event.getNewState() == WorldState.WORLD) scan();
   }

   @SubscribeEvent(priority = EventPriority.LOW)
   public void onContainerSetContents(ContainerSetContentEvent.Pre event) {
      ContainerHelper.getOpened().ifPresent(contents -> {
         if (contents.containerId() == event.getContainerId() &&
                 DIPLOMACY_MENU.matcher(ChatUtil.strip(contents.title())).matches()
         ) update(event.getItems());
      });
   }

   @SubscribeEvent
   public void onChatReceived(ChatMessageReceivedEvent event) {
      var text = event.getOriginalStyledText();
      final Matcher[] matcher = new Matcher[1];
      matcher[0] = text.getMatcher(TRIBUTES_SENT_PATTERN, PartStyle.StyleType.NONE);

      if (matcher[0].matches()) {
         boolean isSelf = matcher[0].group("receiver").equals(Models.Guild.getGuildName());

         TRIBUTES.computeIfAbsent(key(
                 matcher[0].group("sender"),
                 matcher[0].group("receiver")
         ), Tributes::empty).compute(
                 ResourceType.of(matcher[0].group("resource")),
                 (r, other) -> {
                    long amount = Long.parseLong(matcher[0].group("amount"));

                    if (isSelf) return Tributes.Entry.setReceived(other, amount);
                    else return Tributes.Entry.setSent(other, amount);
                 }
         );
      } else if ((matcher[0] = text.getMatcher(TRIBUTES_CANCELLED_PATTERN, PartStyle.StyleType.NONE)).matches()) {
         boolean isSelf = matcher[0].group("receiver").equals(Models.Guild.getGuildName());

         TRIBUTES.computeIfPresent(key(
                 matcher[0].group("sender"),
                 matcher[0].group("receiver")
         ), (str, guild) -> {
            guild.computeIfPresent(ResourceType.of(matcher[0].group("resource")),
                    (resource, other) -> {
                       if (isSelf) return Tributes.Entry.setReceived(other, 0);
                       else return Tributes.Entry.setSent(other, 0);
                    });

            return guild;
         });

         cleanup();
      }
   }

   private String key(String sender, String receiver) {
      if (receiver.equals(Models.Guild.getGuildName())) return sender;

      return receiver;
   }

   private void cleanup() {
      TRIBUTES.values().removeIf(tributes -> tributes.values().stream()
              .anyMatch(entry -> entry.sent() == 0 && entry.received() == 0)
      );
   }

   private void update(List<ItemStack> contents) {
      TRIBUTES.clear();
      for (ItemStack stack : contents)
         Tributes.from(stack).ifPresent(tributes -> TRIBUTES.put(tributes.guild(), tributes));
   }

   private void scan() {
      ScriptedContainerQuery.builder("Tributes Scanner")
              .then(QueryStep.sendCommand("gu manage")
                      .expectContainerTitle(Patterns.GUILD_MANAGE_MENU.pattern())
              ).then(QueryStep.clickOnSlot(26)
                      .expectContainerTitle(DIPLOMACY_MENU.pattern()))
              .reprocess(contents -> update(contents.items()))
              .build().executeQuery();
   }

   public static Map<ResourceType, Long> getNetTributes() {
      Map<ResourceType, Long> map = new HashMap<>();

      TRIBUTES.values().forEach(guild -> guild.forEach((key, value) -> map.compute(key, (r, old) -> {
         long sum = -value.sent() + value.received();

         if (old == null) return sum;
         else return old + sum;
      })));

      return map;
   }
}
