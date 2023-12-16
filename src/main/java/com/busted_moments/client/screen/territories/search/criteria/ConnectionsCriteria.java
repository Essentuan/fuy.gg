package com.busted_moments.client.screen.territories.search.criteria;

import com.busted_moments.client.screen.territories.search.Criteria;
import com.busted_moments.client.screen.territories.search.Operator;
import com.busted_moments.client.screen.territories.search.Operators;

import java.util.List;

@Criteria.With("connections")
@Operators({Operator.EQUALS, Operator.IS, Operator.GREATER_THAN, Operator.GREATER_THAN_OR_EQUALS, Operator.LESS_THAN, Operator.LESS_THAN_OR_EQUALS})
public class ConnectionsCriteria extends Criteria {
   private static final List<String> SUGGESTIONS = List.of("1", "2", "3", "4", "5", "6");

   public ConnectionsCriteria(Operator operator) throws UnsupportedOperationException {
      super(operator);
   }

   @Override
   public List<String> suggestions() {
      return SUGGESTIONS;
   }

   @Override
   public Compiled compile(String value) {
      return new Compiled(this, value, operator().comparing(t -> t.getConnections().size(), Integer.parseInt(value)));
   }
}