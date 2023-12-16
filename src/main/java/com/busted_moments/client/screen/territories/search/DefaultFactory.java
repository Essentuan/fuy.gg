package com.busted_moments.client.screen.territories.search;

import com.busted_moments.core.UnexpectedException;
import com.busted_moments.core.util.EnumUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public record DefaultFactory(Class<? extends Criteria> cls, String prefix, Set<Operator> operators) implements Criteria.Factory {
   public DefaultFactory(Class<? extends Criteria> cls) {
      this(
              cls,
              cls.getAnnotation(Criteria.With.class).value(),
              EnumUtil.asSet(cls.getAnnotation(Operators.class).value(), Operator.class)
      );
   }

   @Override
   public String prefix() {
      return cls.getAnnotation(Criteria.With.class).value();
   }

   @Override
   public Criteria create(Operator operator) {
      try {
         return cls.getConstructor(Operator.class).newInstance(operator);
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
         throw UnexpectedException.propagate(e);
      }
   }
}
