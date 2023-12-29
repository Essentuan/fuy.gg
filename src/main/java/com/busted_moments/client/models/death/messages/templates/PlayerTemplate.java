package com.busted_moments.client.models.death.messages.templates;

import com.busted_moments.client.models.death.messages.DeathMessage;
import com.busted_moments.client.models.death.messages.Target;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

public class PlayerTemplate extends BaseModel implements DeathMessage.Template {
   @Key private Set<String> players;
   @Key private String template;

   @Key private boolean strict;

   public PlayerTemplate(Matcher matcher) {
      this.players = Arrays.stream(matcher.group("players").split(";"))
              .map(String::toLowerCase)
              .map(s -> s.replace(" ", ""))
              .collect(Collectors.toSet());
      this.template = matcher.group("template");

      this.strict = !matcher.group("strict").isEmpty();
   }

   public PlayerTemplate() {}

   public Set<String> players() {
      return players;
   }

   public boolean isStrict() {
      return strict;
   }

   @Override
   public DeathMessage format(Target target) {
      if (!players.contains(target.username().toLowerCase()))
         return DeathMessage.empty(target);

      return new Result(target);
   }

   private class Result implements DeathMessage {
      private final Target target;

      Result(Target target) {
         this.target = target;
      }

      @Override
      public Target target() {
         return target;
      }

      @Override
      public StyledText build() {
         return TextBuilder.empty()
                 .append(List.of(Managers.Function.doFormatLines(template)), (text, builder) -> builder.append(text))
                 .build();
      }
   }
}
