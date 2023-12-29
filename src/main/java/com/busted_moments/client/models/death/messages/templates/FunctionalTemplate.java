package com.busted_moments.client.models.death.messages.templates;

import com.busted_moments.client.models.death.messages.DeathMessage;
import com.busted_moments.client.models.death.messages.Target;
import com.busted_moments.core.json.BaseModel;
import com.busted_moments.core.text.TextBuilder;
import com.wynntils.core.components.Managers;
import com.wynntils.core.text.StyledText;

import java.util.List;

public class FunctionalTemplate extends BaseModel implements DeathMessage.Template {
   @Key private String template;

   public FunctionalTemplate(String template) {
      this.template = template;
   }

   public FunctionalTemplate() {}

   @Override
   public DeathMessage format(Target target) {
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
