package com.busted_moments.client.screen.territories.search.criteria;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.screen.territories.search.Criteria;
import com.busted_moments.client.screen.territories.search.Operator;
import com.busted_moments.client.screen.territories.search.Operators;
import com.busted_moments.core.util.EnumUtil;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Criteria.With("is")
@Operators(Operator.IS)
public class IsCriteria extends Criteria {
   private static final List<String> SUGGESTIONS = Stream.of(Type.values())
           .map(Enum::name)
           .map(String::toLowerCase)
           .toList();

   public IsCriteria(Operator operator) throws UnsupportedOperationException {
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
      HQ(TerritoryEco::isHQ),
      CONNECTION(TerritoryEco::isConnection),
      EXTERNAL(TerritoryEco::isExternal);

      private final Predicate<TerritoryEco> predicate;

      Type(Predicate<TerritoryEco> predicate) {
         this.predicate = predicate;
      }

      @Override
      public boolean test(TerritoryEco territoryEco) {
         return predicate.test(territoryEco);
      }
   }
}
