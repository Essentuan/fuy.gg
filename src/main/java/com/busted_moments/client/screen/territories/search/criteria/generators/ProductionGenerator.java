package com.busted_moments.client.screen.territories.search.criteria.generators;

import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.client.screen.territories.search.Criteria;
import com.busted_moments.client.screen.territories.search.Operator;
import com.busted_moments.client.screen.territories.search.Operators;
import com.busted_moments.core.util.EnumUtil;
import com.busted_moments.core.util.StringUtil;

import java.util.Set;
import java.util.stream.Stream;

public class ProductionGenerator implements Criteria.Generator {
   @Override
   public Stream<Criteria.Factory> get() {
      return Stream.of(ResourceType.values()).map(Factory::new);
   }

   @Operators({Operator.EQUALS, Operator.IS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUALS})
   public static class Generated extends Criteria.Procedural {
      private final ResourceType resourceType;

      public Generated(String prefix, ResourceType resourceType, Operator operator) throws UnsupportedOperationException {
         super(prefix, operator);

         this.resourceType = resourceType;
      }

      @Override
      public Compiled compile(String value) {
         return new Compiled(this, value, operator().comparing(t -> t.getProduction(resourceType), Long.parseLong(value)));
      }
   }

   public record Factory(String prefix, Set<Operator> operators, ResourceType resourceType) implements Criteria.Factory {
      public Factory(ResourceType resourceType) {
         this(
                 StringUtil.camelCase(resourceType.name()),
                 EnumUtil.asSet(Generated.class.getAnnotation(Operators.class).value(), Operator.class),
                 resourceType
         );
      }

      @Override
      public Class<? extends Criteria> cls() {
         return Generated.class;
      }

      @Override
      public Criteria create(Operator operator) {
         return new Generated(prefix, resourceType, operator);
      }
   }
}
