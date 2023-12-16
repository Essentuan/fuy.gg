package com.busted_moments.client.screen.territories.search.criteria;

import com.busted_moments.client.models.territory.eco.TerritoryEco;
import com.busted_moments.client.screen.territories.search.Criteria;
import com.busted_moments.client.screen.territories.search.Operator;
import com.busted_moments.client.screen.territories.search.Operators;

@Criteria.With("treasury")
@Operators({Operator.EQUALS, Operator.IS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUALS})
public class TreasuryCriteria extends Criteria {
   public TreasuryCriteria(Operator operator) throws UnsupportedOperationException {
      super(operator);
   }

   @Override
   public Compiled compile(String value) {
      return new Compiled(this, value, operator().comparing(TerritoryEco::getTreasury, Double.parseDouble(value)));
   }
}
