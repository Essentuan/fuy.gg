package com.busted_moments.client.models.death.messages;

import com.busted_moments.client.models.death.messages.events.DeathEvent;
import com.busted_moments.client.models.death.messages.functions.PlayFunction;
import com.busted_moments.core.Model;
import com.wynntils.core.text.PartStyle;
import com.wynntils.core.text.StyledText;
import com.wynntils.handlers.chat.event.ChatMessageReceivedEvent;
import com.wynntils.utils.type.IterationDecision;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DeathMessageModel extends Model {
   private static Target target;

   @SubscribeEvent(priority = EventPriority.HIGHEST)
   @SuppressWarnings("DataFlowIssue")
   public void onMessage(ChatMessageReceivedEvent event) {
      Optional<DeathMessage> message = DeathMessage.find(event.getStyledText());

      if (message.isEmpty())
         return;

      PlayFunction.enabled = false;

      DeathEvent deathEvent = new DeathEvent(message.get());

      target = deathEvent.target();

      if (deathEvent.post()) {
         event.setCanceled(true);
         return;
      }

      if (deathEvent.message().build().isEmpty()) {
         PlayFunction.enabled = true;
         return;
      }

      PlayFunction.enabled = true;

      StyledText text = deathEvent.message().build();

      if (target.nickname().isPresent())
         text = text.iterate((next, changes) -> {
            if (!next.getString(null, PartStyle.StyleType.NONE).trim().equals(target.nickname().get().trim()))
               return IterationDecision.CONTINUE;

            changes.clear();
            changes.add(target.displayName().getFirstPart());

            return IterationDecision.CONTINUE;
         });

      event.setMessage(text);

      target = null;
   }

   public static @Nullable Target target() {
      return target;
   }
}
