package com.busted_moments.client.screen.territories.search.criteria;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.models.territory.eco.types.ResourceType;
import com.busted_moments.client.screen.territories.search.Criteria;
import com.busted_moments.client.screen.territories.search.Operator;
import com.busted_moments.client.screen.territories.search.Operators;
import com.busted_moments.core.util.EnumUtil;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Operators(Operator.IS)
@Criteria.With("produces")
public class ProductionCriteria extends Criteria {
   private static final List<String> SUGGESTIONS = Stream.of(Type.values())
           .map(Enum::name)
           .map(String::toLowerCase)
           .toList();

   public ProductionCriteria(Operator operator) throws UnsupportedOperationException {
      super(operator);
   }

   @Override
   public Compiled compile(String value) {
      return new Compiled(this, value, EnumUtil.valueOf(value, Type.class).orElseThrow());
   }

   @Override
   public List<String> suggestions() {
      return SUGGESTIONS;
   }

   private enum Type implements Predicate<TerritoryEco> {
      EMERALDS(ResourceType.EMERALDS),
      ORE(ResourceType.ORE),
      WOOD(ResourceType.WOOD),
      FISH(ResourceType.FISH),
      CROP(ResourceType.CROP),
      RAINBOW(t -> {
         for (ResourceType resourceType : ResourceType.values())
            if (t.getBaseProduction(resourceType) == 0)
               return false;

         return true;
      });

      private final Predicate<TerritoryEco> predicate;

      Type(ResourceType resourceType) {
         if (resourceType == ResourceType.EMERALDS)
            predicate = t -> t.getBaseProduction(resourceType) > 9000;
         else
            predicate = t -> t.getBaseProduction(resourceType) > 0;
      }

      Type(Predicate<TerritoryEco> predicate) {
         this.predicate = predicate;
      }

      @Override
      public boolean test(TerritoryEco territoryEco) {
         return predicate.test(territoryEco);
      }
   }
}
